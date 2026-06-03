let isPanelOpen = false;

function toggleAssistant() {
    const panel = document.getElementById('assistant-panel');
    isPanelOpen = !isPanelOpen;
    
    if (isPanelOpen) {
        panel.style.display = 'block';
        loadInterestedPoints();
    } else {
        panel.style.display = 'none';
    }
}

function switchTab(tabName) {
    const tabs = document.querySelectorAll('.tab-content');
    const buttons = document.querySelectorAll('.tab-btn');
    
    tabs.forEach(tab => tab.classList.remove('active'));
    buttons.forEach(btn => btn.classList.remove('active'));
    
    document.getElementById('tab-' + tabName).classList.add('active');
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    
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
    const chatMessages = document.getElementById('chat-messages');
    
    input.value = '';
    sendBtn.disabled = true;
    sendBtn.querySelector('.send-text').style.display = 'none';
    sendBtn.querySelector('.send-loading').style.display = 'inline';
    
    addUserMessage(message);
    
    try {
        const response = await fetch('/assistant/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ message: message })
        });
        
        const data = await response.json();
        
        if (data.success) {
            if (data.async && data.requestId) {
                addAssistantMessage('📨 请求已提交，AI正在思考中...');
                pollForResponse(data.requestId);
            } else if (data.response) {
                addAssistantMessage(data.response);
            } else {
                addAssistantMessage('抱歉，未收到有效响应');
            }
        } else {
            addAssistantMessage('抱歉，处理请求时出现错误：' + (data.error || '未知错误'));
        }
    } catch (error) {
        console.error('发送消息失败:', error);
        addAssistantMessage('网络错误，请检查连接后重试。');
    } finally {
        sendBtn.disabled = false;
        sendBtn.querySelector('.send-text').style.display = 'inline';
        sendBtn.querySelector('.send-loading').style.display = 'none';
        input.focus();
    }
}

async function pollForResponse(requestId, attempts = 0) {
    const maxAttempts = 30;
    
    if (attempts >= maxAttempts) {
        updateLastMessage('⏰ 响应超时，请稍后重试');
        return;
    }
    
    try {
        const response = await fetch(`/assistant/poll-response?requestId=${requestId}`);
        const data = await response.json();
        
        if (data.completed) {
            if (data.response) {
                removeLastMessage();
                addAssistantMessage(data.response);
                
                if (data.processingTime) {
                    addSystemMessage(`✅ 处理完成 (耗时 ${Math.round(data.processingTime/1000)}s)`);
                }
            } else if (data.graphData) {
                renderKnowledgeGraph(data.graphData);
            } else if (data.error) {
                updateLastMessage('❌ 处理失败: ' + data.error);
            }
        } else {
            setTimeout(() => pollForResponse(requestId, attempts + 1), 1000);
        }
    } catch (error) {
        console.error('轮询失败:', error);
        if (attempts < maxAttempts - 1) {
            setTimeout(() => pollForResponse(requestId, attempts + 1), 2000);
        } else {
            updateLastMessage('❌ 轮询失败，请刷新页面重试');
        }
    }
}

function removeLastMessage() {
    const chatMessages = document.getElementById('chat-messages');
    if (chatMessages.lastChild) {
        chatMessages.removeChild(chatMessages.lastChild);
    }
}

function updateLastMessage(content) {
    const messages = document.querySelectorAll('.message.assistant-message .message-content pre');
    if (messages.length > 0) {
        messages[messages.length - 1].textContent = content;
    }
}

function addSystemMessage(content) {
    const chatMessages = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message assistant-message';
    messageDiv.innerHTML = `
        <div class="message-avatar">ℹ️</div>
        <div class="message-content" style="background:#e8f4f8;color:#0066cc;font-size:12px;">${escapeHtml(content)}</div>
    `;
    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

function addUserMessage(message) {
    const chatMessages = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message user-message';
    messageDiv.innerHTML = `
        <div class="message-avatar">👤</div>
        <div class="message-content">${escapeHtml(message)}</div>
    `;
    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

function addAssistantMessage(content) {
    const chatMessages = document.getElementById('chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message assistant-message';
    messageDiv.innerHTML = `
        <div class="message-avatar">🤖</div>
        <div class="message-content"><pre style="white-space: pre-wrap; margin: 0;">${escapeHtml(content)}</pre></div>
    `;
    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

function scrollToBottom() {
    const chatMessages = document.getElementById('chat-messages');
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

async function markInterest(knowledgePointId, knowledgePointTitle) {
    try {
        const response = await fetch(`/assistant/mark-interest?knowledgePointId=${encodeURIComponent(knowledgePointId)}&knowledgePointTitle=${encodeURIComponent(knowledgePointTitle)}`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            updateBadge(data.totalCount);
            showNotification(data.message || '已添加到兴趣列表', 'success');
            
            if (isPanelOpen) {
                loadInterestedPoints();
            }
        } else {
            showNotification(data.error || '添加失败', 'error');
        }
    } catch (error) {
        console.error('标记失败:', error);
        showNotification('操作失败', 'error');
    }
}

async function removeInterest(knowledgePointId) {
    try {
        const response = await fetch(`/assistant/remove-interest?knowledgePointId=${encodeURIComponent(knowledgePointId)}`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            updateBadge(data.totalCount);
            loadInterestedPoints();
            showNotification(data.message || '已移除', 'success');
        } else {
            showNotification(data.error || '移除失败', 'error');
        }
    } catch (error) {
        console.error('移除失败:', error);
        showNotification('操作失败', 'error');
    }
}

async function loadInterestedPoints() {
    try {
        const response = await fetch('/assistant/get-interested-points');
        const data = await response.json();
        
        if (data.success) {
            renderPointsList(data.points);
            updateBadge(data.totalCount);
        }
    } catch (error) {
        console.error('加载兴趣列表失败:', error);
    }
}

function renderPointsList(points) {
    const container = document.getElementById('interested-points-list');
    
    if (!points || points.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <p>📭 还没有标记任何知识点</p>
                <p class="hint">在问题详情页点击"📌 标记感兴趣"按钮来添加</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = points.map(point => `
        <div class="point-item">
            <div class="point-info">
                <div class="point-title">📚 ${escapeHtml(point.title)}</div>
                <div class="point-time">${formatTime(point.timestamp)}</div>
            </div>
            <button class="remove-point-btn" onclick="removeInterest('${escapeHtml(point.id)}')" title="移除">✕</button>
        </div>
    `).join('');
}

function formatTime(timestamp) {
    if (!timestamp) return '';
    const date = new Date(parseInt(timestamp));
    return date.toLocaleString('zh-CN', { 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function updateBadge(count) {
    const badge = document.getElementById('assistant-badge');
    if (count > 0) {
        badge.textContent = count > 99 ? '99+' : count;
        badge.style.display = 'flex';
    } else {
        badge.style.display = 'none';
    }
}

async function generateKnowledgeGraph() {
    const graphContainer = document.getElementById('graph-container');
    graphContainer.style.display = 'block';
    
    document.getElementById('graph-canvas').innerHTML = '<div style="text-align:center;padding:50px;color:#999;">⏳ 正在生成知识图谱...</div>';
    
    try {
        const response = await fetch('/assistant/generate-graph', { method: 'POST' });
        const data = await response.json();
        
        if (data.success) {
            if (data.async && data.requestId) {
                document.getElementById('graph-canvas').innerHTML = `
                    <div style="text-align:center;padding:50px;color:#667eea;">
                        <div style="font-size:40px;margin-bottom:20px;">📨</div>
                        <p>图谱生成任务已提交</p>
                        <p style="font-size:12px;color:#999;margin-top:10px;">正在通过消息队列处理，请稍候...</p>
                    </div>
                `;
                pollForResponse(data.requestId);
            } else if (data.graph) {
                renderKnowledgeGraph(data.graph);
            } else {
                document.getElementById('graph-canvas').innerHTML = `
                    <div style="text-align:center;padding:50px;color:#dc3545;">
                        ❌ ${escapeHtml(data.error || '生成失败')}
                    </div>
                `;
            }
        } else {
            document.getElementById('graph-canvas').innerHTML = `
                <div style="text-align:center;padding:50px;color:#dc3545;">
                    ❌ ${escapeHtml(data.error || '生成失败')}
                </div>
            `;
        }
    } catch (error) {
        console.error('生成图谱失败:', error);
        document.getElementById('graph-canvas').innerHTML = `
            <div style="text-align:center;padding:50px;color:#dc3545;">
                ❌ 网络错误，请重试
            </div>
        `;
    }
}

function renderKnowledgeGraph(graphData) {
    const canvas = document.getElementById('graph-canvas');
    canvas.innerHTML = '<div id="echarts-graph" style="width:100%;height:100%;"></div>';
    
    const chart = echarts.init(document.getElementById('echarts-graph'));
    
    const nodes = graphData.nodes.map((node, index) => ({
        id: node.id,
        name: node.name,
        category: node.category || '默认',
        symbolSize: Math.max(40, Math.min(70, 30 + index * 5)),
        itemStyle: {
            color: getColorForCategory(node.category),
            borderColor: '#fff',
            borderWidth: 2
        },
        label: {
            show: true,
            fontSize: 12,
            fontWeight: 'bold'
        }
    }));
    
    const links = graphData.links.map(link => ({
        source: link.source,
        target: link.target,
        value: link.relation || 'related-to',
        lineStyle: {
            color: getLineColor(link.relation),
            width: 2,
            curveness: 0.2
        },
        label: {
            show: true,
            formatter: getRelationLabel(link.relation),
            fontSize: 10,
            color: '#666'
        }
    }));
    
    const option = {
        tooltip: {
            trigger: 'item',
            formatter: function(params) {
                if (params.dataType === 'node') {
                    return `<strong>${params.data.name}</strong><br/>分类：${params.data.category}`;
                } else if (params.dataType === 'edge') {
                    return `${params.data.value}<br/>${params.data.description || ''}`;
                }
                return '';
            }
        },
        animationDurationUpdate: 1500,
        animationEasingUpdate: 'quinticInOut',
        series: [{
            type: 'graph',
            layout: 'force',
            data: nodes,
            links: links,
            roam: true,
            draggable: true,
            force: {
                repulsion: 300,
                edgeLength: [150, 250],
                gravity: 0.1
            },
            emphasis: {
                focus: 'adjacency',
                lineStyle: {
                    width: 4
                }
            },
            lineStyle: {
                opacity: 0.7,
                curveness: 0.2
            },
            label: {
                show: true,
                position: 'inside',
                fontSize: 11
            }
        }]
    };
    
    chart.setOption(option);
    
    window.addEventListener('resize', () => chart.resize());
}

function getColorForCategory(category) {
    const colors = {
        '默认': '#667eea',
        'Java': '#f89820',
        'Spring': '#6db33f',
        '数据库': '#00758f',
        '前端': '#e34c26',
        '算法': '#f1e05a',
        '网络': '#438eff',
        '操作系统': '#a07c50'
    };
    return colors[category] || '#' + Math.floor(Math.random()*16777215).toString(16);
}

function getLineColor(relation) {
    const colors = {
        'depends-on': '#ff6b6b',
        'contains': '#4ecdc4',
        'related-to': '#95e1d3',
        'prerequisite-of': '#ffa07a'
    };
    return colors[relation] || '#ddd';
}

function getRelationLabel(relation) {
    const labels = {
        'depends-on': '依赖',
        'contains': '包含',
        'related-to': '相关',
        'prerequisite-of': '前置'
    };
    return labels[relation] || relation;
}

function closeGraph() {
    document.getElementById('graph-container').style.display = 'none';
}

async function clearAllInterests() {
    if (!confirm('确定要清空所有标记的知识点吗？')) return;
    
    try {
        const response = await fetch('/assistant/clear-history', { method: 'POST' });
        const data = await response.json();
        
        if (data.success) {
            loadInterestedPoints();
            showNotification('已清空所有数据', 'success');
        } else {
            showNotification(data.error || '清空失败', 'error');
        }
    } catch (error) {
        console.error('清空失败:', error);
        showNotification('操作失败', 'error');
    }
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 80px;
        right: 30px;
        padding: 12px 24px;
        border-radius: 8px;
        color: white;
        font-size: 14px;
        z-index: 10001;
        animation: slideInRight 0.3s ease;
        background: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#667eea'};
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    notification.textContent = message;
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'fadeOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes fadeOut {
        from { opacity: 1; }
        to { opacity: 0; }
    }
`;
document.head.appendChild(style);

window.addEventListener('load', () => {
    loadInterestedPoints();
});