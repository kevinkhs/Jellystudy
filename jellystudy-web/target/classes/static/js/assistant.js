/**
 * JellyStudy AI 学习助手 — v0 设计版本
 * 悬浮聊天窗口 + 知识图谱功能
 */

let isPanelOpen = false;

function toggleAssistant() {
    const panel = document.getElementById('assistant-panel');
    const toggle = document.getElementById('assistant-toggle');
    isPanelOpen = !isPanelOpen;

    if (isPanelOpen) {
        panel.style.display = 'flex';
        requestAnimationFrame(() => {
            panel.style.transform = 'scale(1)';
            panel.style.opacity = '1';
        });
        toggle.style.transform = 'scale(0)';
        toggle.style.opacity = '0';
        toggle.style.pointerEvents = 'none';
        loadInterestedPoints();
        // Focus input
        setTimeout(() => {
            const input = document.getElementById('user-input');
            if (input) input.focus();
        }, 500);
    } else {
        panel.style.transform = 'scale(0.95)';
        panel.style.opacity = '0';
        setTimeout(() => {
            panel.style.display = 'none';
        }, 400);
        toggle.style.transform = '';
        toggle.style.opacity = '';
        toggle.style.pointerEvents = '';
    }
}

function switchTab(tabName) {
    // 切换内容区
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.style.display = 'none';
    });
    const targetTab = document.getElementById('tab-' + tabName);
    if (targetTab) targetTab.style.display = 'flex';

    // 切换按钮样式
    document.querySelectorAll('.assistant-tab').forEach(btn => {
        btn.classList.remove('active');
        btn.style.color = 'rgba(255,255,255,0.45)';
        btn.style.borderBottomColor = 'transparent';
    });

    const activeBtn = document.getElementById('tab-' + tabName + '-btn');
    if (activeBtn) {
        activeBtn.classList.add('active');
        activeBtn.style.color = '#667eea';
        activeBtn.style.borderBottomColor = '#667eea';
    }

    // 显示/隐藏输入框
    const inputArea = document.getElementById('chat-input-area');
    if (inputArea) {
        inputArea.style.display = tabName === 'chat' ? 'flex' : 'none';
    }

    if (tabName === 'knowledge') {
        loadInterestedPoints();
    }
}

function handleKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
}

async function sendMessage() {
    const input = document.getElementById('user-input');
    const message = input.value.trim();
    if (!message) return;

    const sendBtn = document.getElementById('send-btn');
    input.value = '';
    sendBtn.disabled = true;
    sendBtn.style.opacity = '0.6';

    addUserMessage(message);

    try {
        const response = await fetch('/assistant/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: message })
        });

        const data = await response.json();
        console.log('[AI] Chat response:', data); // 调试日志

        if (data.success) {
            if (data.response) {
                // 同步模式：直接显示响应
                addAssistantMessage(data.response);
                console.log('[AI] 显示同步响应');
            } else if (data.async && data.requestId) {
                // 异步模式：轮询等待响应
                addAssistantMessage('📨 请求已提交，AI 正在思考中...');
                console.log('[AI] 开始轮询, requestId:', data.requestId);
                pollForResponse(data.requestId);
            } else {
                addAssistantMessage('抱歉，未收到有效响应');
                console.warn('[AI] 无响应数据:', data);
            }
        } else {
            addAssistantMessage('抱歉，处理请求时出现错误：' + (data.error || '未知错误'));
            console.error('[AI] 请求失败:', data.error);
        }
    } catch (error) {
        console.error('发送消息失败:', error);
        addAssistantMessage('网络错误，请检查连接后重试。');
    } finally {
        sendBtn.disabled = false;
        sendBtn.style.opacity = '1';
        input.focus();
    }
}

async function pollForResponse(requestId, attempts = 0) {
    const maxAttempts = 30;
    if (attempts >= maxAttempts) {
        updateLastMessage('⏰ 响应超时，请稍后重试');
        console.warn('[AI] 轮询超时');
        return;
    }

    try {
        const response = await fetch(`/assistant/poll-response?requestId=${requestId}`);
        const data = await response.json();
        console.log(`[AI] Poll #${attempts + 1}:`, data); // 调试日志

        if (data.completed) {
            if (data.response) {
                removeLastMessage();
                addAssistantMessage(data.response);
                console.log('[AI] 显示异步响应');
            } else if (data.graphData) {
                renderKnowledgeGraph(data.graphData);
                console.log('[AI] 渲染知识图谱');
            } else if (data.error) {
                updateLastMessage('❌ 处理失败: ' + data.error);
                console.error('[AI] 处理失败:', data.error);
            } else {
                updateLastMessage('✅ 处理完成，但无响应内容');
                console.warn('[AI] 完成但无内容');
            }
        } else {
            setTimeout(() => pollForResponse(requestId, attempts + 1), 1000);
        }
    } catch (error) {
        console.error('[AI] 轮询错误:', error);
        if (attempts < maxAttempts - 1) {
            setTimeout(() => pollForResponse(requestId, attempts + 1), 2000);
        } else {
            updateLastMessage('❌ 轮询失败，请刷新页面重试');
        }
    }
}

function removeLastMessage() {
    const container = document.getElementById('chat-messages-container');
    if (container.lastChild) container.removeChild(container.lastChild);
}

function updateLastMessage(content) {
    const bubbles = document.querySelectorAll('#chat-messages-container .msg-bubble');
    if (bubbles.length > 0) {
        const pre = bubbles[bubbles.length - 1].querySelector('pre');
        if (pre) pre.textContent = content;
        else bubbles[bubbles.length - 1].textContent = content;
    }
}

function addUserMessage(message) {
    const container = document.getElementById('chat-messages-container');
    const row = document.createElement('div');
    row.className = 'msg-row';
    row.style.cssText = 'display:flex; justify-content:flex-end;';
    row.innerHTML = `<div class="msg-bubble msg-user" style="max-width:85%; border-radius:16px; padding:12px 16px; font-size:14px; line-height:1.6;"><pre style="white-space:pre-wrap;margin:0;font-family:inherit;font-size:inherit;">${escapeHtml(message)}</pre></div>`;
    container.appendChild(row);
    scrollToBottom();
}

function addAssistantMessage(content) {
    const container = document.getElementById('chat-messages-container');
    const row = document.createElement('div');
    row.className = 'msg-row';
    row.style.cssText = 'display:flex; justify-content:flex-start;';
    row.innerHTML = `<div class="msg-bubble" style="max-width:85%; border-radius:16px; padding:12px 16px; font-size:14px; line-height:1.6; color:rgba(255,255,255,0.9); background:rgba(255,255,255,0.07);"><pre style="white-space:pre-wrap;margin:0;font-family:inherit;font-size:inherit;">${escapeHtml(content)}</pre></div>`;
    container.appendChild(row);
    scrollToBottom();
}

function scrollToBottom() {
    const chatEl = document.getElementById('tab-chat');
    if (chatEl) chatEl.scrollTop = chatEl.scrollHeight;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ========== 知识图谱相关 ==========

async function markInterest(knowledgePointId, knowledgePointTitle) {
    try {
        const response = await fetch(`/assistant/mark-interest?knowledgePointId=${encodeURIComponent(knowledgePointId)}&knowledgePointTitle=${encodeURIComponent(knowledgePointTitle)}`, { method: 'POST' });
        const data = await response.json();
        showNotification(data.success ? (data.message || '已添加') : (data.error || '添加失败'), data.success ? 'success' : 'error');
        if (isPanelOpen) loadInterestedPoints();
    } catch (e) {
        showNotification('操作失败', 'error');
    }
}

async function removeInterest(knowledgePointId) {
    try {
        const response = await fetch(`/assistant/remove-interest?knowledgePointId=${encodeURIComponent(knowledgePointId)}`, { method: 'POST' });
        const data = await response.json();
        if (data.success) { loadInterestedPoints(); showNotification(data.message || '已移除', 'success'); }
        else { showNotification(data.error || '移除失败', 'error'); }
    } catch (e) {
        showNotification('操作失败', 'error');
    }
}

async function loadInterestedPoints() {
    try {
        // 加载知识点点击统计，显示最近浏览列表
        const response = await fetch('/assistant/graph-data');
        const data = await response.json();

        if (data.success && data.nodes && data.nodes.length > 0) {
            const container = document.getElementById('recent-kp-items');
            if (container) {
                container.innerHTML = '';
                // 只显示最近5个
                const recent = data.nodes.slice(0, 5);
                recent.forEach(node => {
                    const item = document.createElement('a');
                    item.href = '/knowledge/' + node.id;
                    item.target = '_blank';
                    item.style.cssText = 'display:flex; align-items:center; justify-content:space-between; padding:8px 12px; border-radius:8px; background:rgba(255,255,255,0.05); text-decoration:none; transition:all 0.2s; cursor:pointer;';
                    item.onmouseover = function() { this.style.background = 'rgba(102,126,234,0.15)'; };
                    item.onmouseout = function() { this.style.background = 'rgba(255,255,255,0.05)'; };
                    item.innerHTML = '<span style="font-size:13px; color:rgba(255,255,255,0.85); overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:200px;">' + escapeHtml(node.title) + '</span>' +
                        '<span style="font-size:11px; color:rgba(255,255,255,0.4); flex-shrink:0;">' + node.clickCount + '次</span>';
                    container.appendChild(item);
                });
            }
        } else {
            const container = document.getElementById('recent-kp-items');
            if (container) {
                container.innerHTML = '<p style="font-size:12px; color:rgba(255,255,255,0.35);">浏览问题后将自动记录</p>';
            }
        }
    } catch (e) {
        // 静默处理
    }
}

function generateKnowledgeGraph() {
    const kTab = document.getElementById('tab-knowledge');
    if (!kTab) return;
    kTab.innerHTML = `<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#667eea;font-size:14px;"><div style="text-align:center;"><svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="animation:spin 1s linear infinite;"><circle cx="12" cy="12" r="10"/><path d="M9 12l2 2 4-4"/></svg><p style="margin-top:12px;">正在生成知识图谱...</p></div></div>`;
    fetch('/assistant/generate-graph', { method: 'POST' })
        .then(r => r.json())
        .then(data => {
            if (data.success && data.graph) renderKnowledgeGraph(data.graph);
            else kTab.innerHTML = `<div style="display:flex;align-items:center;justify-content:center;height:100%;text-align:center;padding:32px;"><div><h4 style="color:rgba(255,255,255,0.95);margin-bottom:8px;">${data.error || '生成失败'}</h4></div></div>`;
        })
        .catch(() => {
            kTab.innerHTML = `<div style="display:flex;align-items:center;justify-content:center;height:100%;text-align:center;padding:32px;"><h4 style="color:#ef4444;">网络错误</h4></div>`;
        });
}

function renderKnowledgeGraph(graphData) {
    const kTab = document.getElementById('tab-knowledge');
    if (!kTab || !window.echarts) return;
    kTab.innerHTML = '<div id="graph-canvas" style="width:100%;height:100%;"></div>';

    const chart = echarts.init(document.getElementById('graph-canvas'));
    const nodes = graphData.nodes.map((node, i) => ({
        id: node.id, name: node.name,
        symbolSize: Math.max(40, Math.min(70, 30 + i * 5)),
        itemStyle: { color: getColorForCategory(node.category), borderColor: '#fff', borderWidth: 2 },
        label: { show: true, fontSize: 12, fontWeight: 'bold' }
    }));
    const links = graphData.links.map(link => ({
        source: link.source, target: link.target,
        lineStyle: { color: getLineColor(link.relation), width: 2, curveness: 0.2 }
    }));

    chart.setOption({
        tooltip: { trigger: 'item' },
        animationDurationUpdate: 1500,
        series: [{
            type: 'graph', layout: 'force', data: nodes, links: links, roam: true, draggable: true,
            force: { repulsion: 300, edgeLength: [150, 250], gravity: 0.1 },
            emphasis: { focus: 'adjacency', lineStyle: { width: 4 } },
            lineStyle: { opacity: 0.7, curveness: 0.2 },
            label: { show: true, position: 'inside', fontSize: 11 }
        }]
    });
    window.addEventListener('resize', () => chart.resize());
}

function getColorForCategory(c) {
    const m = { '默认': '#667eea', 'Java': '#f89820', 'Spring': '#6db33f', '数据库': '#00758f', '前端': '#e34c26', '算法': '#f1e05a', '网络': '#438eff' };
    return m[c] || '#667eea';
}
function getLineColor(r) { const m = { 'depends-on': '#ff6b6b', 'contains': '#4ecdc4', 'related-to': '#95e1d3' }; return m[r] || '#aaa'; }

function clearAllInterests() {
    if (!confirm('确定要清空所有感兴趣的知识点吗？')) return;
    fetch('/assistant/clear-history', { method: 'POST' })
        .then(r => r.json())
        .then(d => { showNotification(d.success ? '已清空' : (d.error || '操作失败'), d.success ? 'success' : 'error'); })
        .catch(() => showNotification('操作失败', 'error'));
}

function showNotification(msg, type) {
    const n = document.createElement('div');
    n.style.cssText = `position:fixed; top:80px; right:30px; padding:12px 24px; border-radius:10px; color:white; font-size:14px; font-weight:500; z-index:10001; background:${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#667eea'}; box-shadow:0 4px 15px rgba(0,0,0,0.2); animation:slideInRight 0.3s ease;`;
    n.textContent = msg;
    document.body.appendChild(n);
    setTimeout(() => { n.style.animation = 'fadeOut 0.3s ease'; setTimeout(() => n.remove(), 300); }, 3000);
}

// 注入动画样式
(function() {
    const s = document.createElement('style');
    s.textContent = `
        @keyframes slideInRight { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
        @keyframes fadeOut { from { opacity: 1; } to { opacity: 0; } }
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
    `;
    document.head.appendChild(s);
})();

// 页面加载时初始化
window.addEventListener('load', () => { loadInterestedPoints(); });
