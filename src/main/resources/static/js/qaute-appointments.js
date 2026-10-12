(() => {
    'use strict';
    let root=document.querySelector('[data-appointment-live-root]');
    if (!root) return;
    const context=root.dataset.contextPath || '';
    document.addEventListener('show.bs.modal',event=>{
        if(event.target.id!=='officeRequestModal') return;
        const trigger=event.relatedTarget;
        const input=event.target.querySelector('[data-office-start-input]');
        const label=event.target.querySelector('[data-office-selected-time]');
        if(input) input.value=trigger?.dataset.officeStart || '';
        if(label) label.textContent=trigger?.dataset.officeLabel || '';
    });
    let busy=false, pending=false, stopped=false, lastRefresh=Date.now(), connected=false, refreshController, debounce;
    function scheduleRefresh() { pending=true; clearTimeout(debounce); debounce=setTimeout(refresh, 500); }
    // Do not let an old refresh race a form POST or consume its redirect flash message.
    document.addEventListener('submit',event=>{
        if(root.contains(event.target)) { stopped=true; refreshController?.abort(); }
    });
    function status(message) { const node=root.querySelector('[data-appointment-live-status]'); if(node) node.textContent=message; }
    function editing() {
        return document.querySelector('.modal.show, .modal.showing') || root.querySelector('details[open]') || root.contains(document.activeElement) && document.activeElement.matches('input, textarea, select');
    }
    async function refresh() {
        if (busy || stopped || document.hidden || editing()) { pending=true; return; }
        busy=true; pending=false;
        try {
            refreshController=new AbortController();
            const url=new URL(location.href); url.pathname=url.pathname.replace(/\/$/,'')+'/live';
            const response=await fetch(url.toString(),{credentials:'same-origin',cache:'no-store',signal:refreshController.signal,headers:{'X-Requested-With':'XMLHttpRequest'}});
            if(response.status===401 || response.status===403 || response.redirected && new URL(response.url).pathname.endsWith('/login')) {
                stopped=true; status('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'); return;
            }
            if(!response.ok) throw new Error('refresh');
            const doc=new DOMParser().parseFromString(await response.text(),'text/html');
            const fresh=doc.querySelector('[data-appointment-live-root]');
            if(!fresh) throw new Error('content');
            if(stopped) return;
            if(editing()) { pending=true; return; }
            const active=root.querySelector('.tab-pane.active')?.id;
            // Dispose old Bootstrap objects before replacing their markup.
            root.querySelectorAll('.modal').forEach(node=>window.bootstrap?.Modal.getInstance(node)?.dispose());
            root.querySelectorAll('[data-bs-toggle="tab"]').forEach(node=>window.bootstrap?.Tab.getInstance(node)?.dispose());
            root.replaceWith(fresh); root=fresh;
            if(active) {
                const tab=root.querySelector('[data-bs-target="#'+active+'"]');
                if(tab && window.bootstrap) window.bootstrap.Tab.getOrCreateInstance(tab).show();
            }
            lastRefresh=Date.now();
            status((connected?'Đang cập nhật trực tiếp':'Cập nhật tự động dự phòng')+' · '+new Date().toLocaleTimeString('vi-VN'));
        } catch (_) { pending=false; lastRefresh=Date.now(); connected=false; status('Chưa cập nhật được lịch. Hệ thống sẽ tự thử lại.'); }
        finally { busy=false; }
    }
    if(new URL(location.href).searchParams.get('tab')==='book') {
        const tab=root.querySelector('[data-bs-target="#book-appointment"]');
        if(tab && window.bootstrap) window.bootstrap.Tab.getOrCreateInstance(tab).show();
    }
    if(window.StompJs && window.SockJS) {
        const client=new window.StompJs.Client({webSocketFactory:()=>new window.SockJS(context+'/ws'),reconnectDelay:5000});
        client.onConnect=()=>{ connected=true; client.subscribe('/queue/appointments',scheduleRefresh); scheduleRefresh(); };
        client.onWebSocketClose=()=>{ connected=false; status('Đang kết nối lại; lịch vẫn tự cập nhật dự phòng.'); };
        client.onStompError=()=>{ connected=false; };
        client.activate();
        window.addEventListener('pagehide',()=>client.deactivate());
    } else status('Cập nhật tự động dự phòng.');
    setInterval(()=>{ if(pending || !connected && Date.now()-lastRefresh>30000) refresh(); },5000);
    document.addEventListener('visibilitychange',()=>{ if(!document.hidden) refresh(); });
    document.addEventListener('hidden.bs.modal',()=>{ if(pending) refresh(); });
})();
