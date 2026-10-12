(() => {
    const form=document.getElementById('faqViewToken');
    if(!form) return;
    const recorded=new Set();
    document.addEventListener('shown.bs.collapse',async event=>{
        const node=event.target, id=node.dataset.faqId;
        if(!id || recorded.has(id)) return;
        recorded.add(id);
        try {
            const response=await fetch(form.dataset.context+'/faq/'+encodeURIComponent(id)+'/view',{
                method:'POST',credentials:'same-origin',body:new URLSearchParams(new FormData(form))
            });
            if(!response.ok) throw new Error();
            const result=await response.json();
            const counter=node.querySelector('[data-faq-views]');
            if(counter) counter.textContent=String(result.views);
        } catch (_) { recorded.delete(id); }
    });
})();
