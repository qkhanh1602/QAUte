(() => {
    'use strict';
    const stream = document.getElementById('messageStream') || document.getElementById('advisorMessageStream');
    if (!stream) return;
    const form = document.getElementById('chatSendForm') || document.getElementById('advisorChatSendForm');
    const input = form.querySelector('[name=noiDung]');
    const button = form.querySelector('[type=submit]');
    document.querySelectorAll('[data-quick-question]').forEach(button => button.addEventListener('click', () => {
        input.value = button.dataset.quickQuestion; input.focus();
    }));
    const presence = document.getElementById('advisorPresence');
    const seen = new Set([...stream.querySelectorAll('[data-message-id]')].map(e => e.dataset.messageId));
    const status = document.createElement('div');
    status.className = 'visually-hidden';
    status.setAttribute('role', 'status');
    status.setAttribute('aria-live', 'polite');
    form.parentElement.prepend(status);
    let client, pending = null, pendingViaSocket = false, ackTimer, refreshing = false;
    const sessionId = Number(stream.dataset.sessionId);
    const userId = Number(stream.dataset.userId);
    const scroll = () => { stream.scrollTop = stream.scrollHeight; };
    const finish = error => {
        clearTimeout(ackTimer);
        if (!error && pending !== null && input.value.trim() === pending) input.value = '';
        pending = null; pendingViaSocket = false;
        button.disabled = false;
        status.textContent = error || (client?.connected ? 'Đã kết nối trực tuyến' : 'Đang đồng bộ tin nhắn');
        status.className = error ? 'small text-danger px-3 py-2' : 'visually-hidden';
    };
    const append = message => {
        if (Number(message.sessionId) !== sessionId || seen.has(String(message.messageId))) return;
        seen.add(String(message.messageId));
        if (!stream.querySelector('[data-message-id]')) stream.replaceChildren();
        const own = Number(message.senderId) === userId;
        const wrap = document.createElement('div');
        wrap.dataset.messageId = String(message.messageId);
        wrap.className = own ? 'd-flex justify-content-end' : 'd-flex justify-content-start';
        const bubble = document.createElement('div');
        bubble.className = own ? 'chat-bubble-sender' : 'chat-bubble-receiver';
        const sender = document.createElement('div'); sender.className = 'small fw-bold mb-1 opacity-75'; sender.textContent = message.senderName;
        const content = document.createElement('div'); content.style.whiteSpace = 'pre-line'; content.textContent = message.content;
        const time = document.createElement('div'); time.className = 'small text-end mt-1 opacity-75';
        time.textContent = String(message.sentAt || '').slice(11, 16);
        if (message.senderName) bubble.append(sender);
        bubble.append(content, time); wrap.append(bubble); stream.append(wrap); scroll();
        if (own && pendingViaSocket && pending === message.content) finish();
    };
    // Same authorized MVC page supplies fallback history and attachment markup.
    const mergePage = html => {
        const page = new DOMParser().parseFromString(html, 'text/html');
        const remote = page.getElementById(stream.id);
        if (!remote || Number(remote.dataset.sessionId) !== sessionId) throw new ChatError('Không đồng bộ được phiên chat. Vui lòng kiểm tra đăng nhập.');
        let added = false;
        remote.querySelectorAll('[data-message-id]').forEach(row => {
            const id = row.dataset.messageId;
            if (seen.has(id)) {
                // Refresh saved markup (including attachments), without duplicating a live message.
                const current = [...stream.children].find(e => e.dataset.messageId === id);
                if (current) current.replaceWith(document.importNode(row, true));
                return;
            }
            if (!stream.querySelector('[data-message-id]')) stream.replaceChildren();
            stream.append(document.importNode(row, true)); seen.add(id); added = true;
        });
        if (added) scroll();
        return page.querySelector('.alert-danger')?.textContent.trim();
    };
    class ChatError extends Error {}
    const userMessage = error => error instanceof ChatError ? error.message : 'Kết nối chat gặp lỗi. Vui lòng thử lại; nội dung đang nhập vẫn được giữ.';
    const checkResponse = response => {
        if (response.status === 401 || (response.redirected && new URL(response.url).pathname.endsWith('/login'))) {
            throw new ChatError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để tiếp tục chat.');
        }
        if (response.status === 403) throw new ChatError('Không có quyền thực hiện thao tác này. Hãy đăng nhập lại hoặc kiểm tra phiên chat.');
        if (!response.ok) throw new ChatError('Không đồng bộ được phiên chat. Vui lòng thử lại sau.');
    };
    const receive = (frame, handler) => {
        try { handler(JSON.parse(frame.body)); }
        catch (error) { status.textContent = 'Chưa nhận được dữ liệu chat hợp lệ. Hệ thống đang đồng bộ lại.'; refresh(); }
    };
    const refresh = async () => {
        if (refreshing || document.hidden) return;
        refreshing = true;
        try {
            const response = await fetch((stream.dataset.contextPath || '') + '/api/chat/' + sessionId + '/messages', { credentials: 'same-origin', cache: 'no-store' });
            checkResponse(response);
            if (!(response.headers.get('content-type') || '').includes('application/json')) {
                throw new ChatError('Chưa đồng bộ được chat. Vui lòng tải lại trang; nếu vẫn gặp lỗi, cần khởi động lại ứng dụng với code mới.');
            }
            const messages = await response.json();
            if (!Array.isArray(messages)) throw new ChatError('Dữ liệu chat chưa hợp lệ. Vui lòng tải lại trang.');
            messages.forEach(message => {
                append(message);
                const row = [...stream.children].find(e => e.dataset.messageId === String(message.messageId));
                if (!row) return;
                (message.attachments || []).forEach(file => {
                    const href = (stream.dataset.contextPath || '') + '/attachments/' + file.id;
                    if ([...row.querySelectorAll('a')].some(a => a.getAttribute('href') === href)) return;
                    const link = document.createElement('a'); link.className = 'btn btn-sm btn-light border mt-2 me-1';
                    link.href = href; link.target = '_blank'; link.rel = 'noopener'; link.textContent = file.name;
                    row.firstElementChild.append(link);
                });
            });
        } catch (error) { status.textContent = userMessage(error); status.classList.add('text-danger'); }
        finally { refreshing = false; }
    };
    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (pending !== null) return;
        const file = form.querySelector('[type=file]');
        const text = input.value.trim();
        if (!text && !file.files.length) return;
        pending = text; button.disabled = true; status.classList.remove('text-danger'); status.textContent = 'Đang gửi…';
        if (!file.files.length && client?.connected) {
            pendingViaSocket = true;
            ackTimer = setTimeout(() => { finish('Chưa nhận được xác nhận gửi. Nội dung vẫn được giữ lại; hãy kiểm tra lịch sử trước khi gửi lại.'); refresh(); }, 12000);
            try { client.publish({ destination: '/app/chat.send', body: JSON.stringify({ sessionId, content: text }) }); }
            catch (error) { finish('Mất kết nối. Tin nhắn chưa được xác nhận.'); }
            return;
        }
        try {
            const response = await fetch(form.action, { method: 'POST', body: new FormData(form), credentials: 'same-origin' });
            checkResponse(response);
            const error = mergePage(await response.text());
            if (error) throw new ChatError(error);
            file.value = ''; finish();
        } catch (error) { finish(userMessage(error)); }
    });
    if (window.SockJS && window.StompJs) {
        client = new StompJs.Client({ webSocketFactory: () => new SockJS((stream.dataset.contextPath || '') + '/ws'), reconnectDelay: 5000 });
        client.onConnect = () => {
            client.subscribe('/user/queue/chat', frame => receive(frame, append));
            client.subscribe('/user/queue/chat-errors', frame => receive(frame, message => finish(message.message || 'Không gửi được tin nhắn. Vui lòng thử lại.')));
            if (presence) client.subscribe('/queue/presence', frame => receive(frame, event => {
                if (event.username === presence.dataset.username) presence.textContent = event.status === 'Trực tuyến' && presence.dataset.busy === 'true' ? 'Đang bận' : event.status;
            }));
            status.textContent = 'Đã kết nối trực tuyến'; refresh();
        };
        client.onWebSocketClose = () => { status.textContent = 'Đang kết nối lại; tin nhắn vẫn được đồng bộ'; };
        client.onStompError = () => { finish('Kết nối chat gặp lỗi. Vui lòng thử lại.'); };
        client.activate();
    }
    setInterval(refresh, 5000);
    document.addEventListener('visibilitychange', () => { if (!document.hidden) refresh(); });
    window.addEventListener('pagehide', () => client?.deactivate());
    scroll();
})();
