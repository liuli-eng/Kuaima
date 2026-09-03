// Admin shared JavaScript
const adminPages = [
    { id: 'dashboard', name: '数据概览', icon: 'fa-chart-line', group: 'dashboard' },
    { id: 'workers', name: '零工管理', icon: 'fa-user', group: 'user' },
    { id: 'bosses', name: '老板管理', icon: 'fa-building', group: 'user' },
    { id: 'jobs', name: '招工管理', icon: 'fa-briefcase', group: 'job' },
    { id: 'job-audit', name: '招工审核', icon: 'fa-check-circle', group: 'job' },
    { id: 'job-edit', name: '编辑招工', icon: 'fa-edit', group: 'job', parent: 'jobs' },
    { id: 'orders', name: '用工订单', icon: 'fa-clipboard-list', group: 'order' },
    { id: 'settlement', name: '结算管理', icon: 'fa-coins', group: 'order' },
    { id: 'finance', name: '财务报表', icon: 'fa-chart-pie', group: 'finance' },
    { id: 'points', name: '积分管理', icon: 'fa-star', group: 'finance' },
    { id: 'certification', name: '认证审核', icon: 'fa-id-card', group: 'content' },
    { id: 'certification-detail', name: '认证详情', icon: 'fa-eye', group: 'content', parent: 'certification' },
    { id: 'banners', name: 'Banner管理', icon: 'fa-image', group: 'content' },
    { id: 'notices', name: '公告管理', icon: 'fa-bullhorn', group: 'content' },
    { id: 'rules', name: '规则管理', icon: 'fa-book', group: 'content' },
    { id: 'rules-edit', name: '新增规则', icon: 'fa-edit', group: 'content', parent: 'rules' },
    { id: 'messages', name: '消息管理', icon: 'fa-envelope', group: 'message' },
    { id: 'service', name: '客服管理', icon: 'fa-headset', group: 'message' },
    { id: 'service-chat', name: '会话处理', icon: 'fa-comments', group: 'message', parent: 'service' },
    { id: 'service-new', name: '新建会话', icon: 'fa-plus-circle', group: 'message', parent: 'service' },
    { id: 'reports', name: '举报处理', icon: 'fa-ban', group: 'risk' },
    { id: 'blacklist', name: '黑名单管理', icon: 'fa-user-times', group: 'risk' },
    { id: 'settings', name: '系统设置', icon: 'fa-cog', group: 'system' },
    { id: 'admin-user', name: '管理员管理', icon: 'fa-user-cog', group: 'system', parent: 'settings' },
    { id: 'logs', name: '操作日志', icon: 'fa-file-alt', group: 'system' },
    { id: 'profile', name: '个人资料', icon: 'fa-user-circle', group: 'system' },
    { id: 'password', name: '修改密码', icon: 'fa-key', group: 'system' },
    { id: 'account', name: '账户设置', icon: 'fa-user-cog', group: 'system' }
];

const menuGroups = [
    { id: 'dashboard', name: '数据统计', items: ['dashboard'] },
    { id: 'user', name: '用户管理', items: ['workers', 'bosses'] },
    { id: 'job', name: '招工管理', items: ['jobs', 'job-applicants', 'job-audit'] },
    { id: 'order', name: '订单结算', items: ['orders', 'settlement'] },
    { id: 'content', name: '内容管理', items: ['certification', 'notices', 'rules'] },
    { id: 'message', name: '消息客服', items: ['messages', 'service'] },
    { id: 'system', name: '系统管理', items: ['settings', 'logs'] }
];

function getPageList() {
    return adminPages;
}

function getMenuGroups() {
    return menuGroups;
}

function navigateTo(pageId) {
    window.location.href = pageId + '.html';
}

function getCurrentPage() {
    const path = window.location.pathname;
    const file = path.split('/').pop().replace('.html', '');
    return file;
}

function initSidebar(currentPage) {
    const sidebar = document.querySelector('.admin-sidebar');
    if (!sidebar) return;
    
    let menuHTML = `
        <div class="sidebar-header">
            <div class="sidebar-logo">
                <i class="fas fa-bolt"></i>
            </div>
            <div>
                <div class="sidebar-title">快马日结</div>
                <div class="sidebar-version">管理后台 v2.0</div>
            </div>
        </div>
        <nav class="sidebar-menu">
    `;
    
    menuGroups.forEach(group => {
        menuHTML += `<div class="menu-group-title">${group.name}</div>`;
        group.items.forEach(itemId => {
            const page = adminPages.find(p => p.id === itemId);
            if (page) {
                const currentPageData = adminPages.find(p => p.id === currentPage);
                const isActive = currentPage === itemId || (currentPageData && currentPageData.parent === itemId);
                const activeClass = isActive ? ' active' : '';
                menuHTML += `
                    <a href="${itemId}.html" class="menu-item${activeClass}">
                        <i class="fas ${page.icon}"></i>
                        <span>${page.name}</span>
                    </a>
                `;
            }
        });
    });
    
    menuHTML += '</nav>';
    sidebar.innerHTML = menuHTML;
}

function initTopbar(currentPage) {
    const topbar = document.querySelector('.admin-topbar');
    if (!topbar) return;
    
    const page = adminPages.find(p => p.id === currentPage);
    const pageName = page ? page.name : '未知页面';
    
    let breadcrumbHTML = `<span>首页</span>`;
    if (page && page.parent) {
        const parent = adminPages.find(p => p.id === page.parent);
        if (parent) {
            breadcrumbHTML += `<span class="breadcrumb-separator"><i class="fas fa-chevron-right" style="font-size:10px;"></i></span>`;
            breadcrumbHTML += `<span><a href="${parent.id}.html" style="color:var(--text-secondary);text-decoration:none;">${parent.name}</a></span>`;
        }
    }
    if (page) {
        breadcrumbHTML += `<span class="breadcrumb-separator"><i class="fas fa-chevron-right" style="font-size:10px;"></i></span>`;
        breadcrumbHTML += `<span class="breadcrumb-current">${pageName}</span>`;
    }
    
    topbar.innerHTML = `
        <div class="topbar-left">
            <div class="breadcrumb">
                ${breadcrumbHTML}
            </div>
        </div>
        <div class="topbar-right">
            <div class="search-box">
                <i class="fas fa-search"></i>
                <input type="text" placeholder="搜索功能...">
            </div>
            <div class="icon-btn" title="通知">
                <i class="fas fa-bell"></i>
                <span class="badge"></span>
            </div>
            <div class="icon-btn" title="帮助">
                <i class="fas fa-question-circle"></i>
            </div>
            <div class="user-info" id="userInfoBtn">
                <div class="user-avatar">管</div>
                <div>
                    <div class="user-name">管理员</div>
                    <div class="user-role">超级管理员</div>
                </div>
                <i class="fas fa-chevron-down" style="font-size:10px;color:var(--text-muted);"></i>
                <div class="user-dropdown" id="userDropdown">
                    <div class="user-dropdown-header">
                        <div class="dropdown-avatar">管</div>
                        <div class="dropdown-info">
                            <div class="dropdown-name">管理员</div>
                            <div class="dropdown-role">超级管理员</div>
                        </div>
                    </div>
                    <button class="user-dropdown-item" onclick="handleDropdownAction('profile')">
                        <i class="fas fa-user"></i>
                        <span>个人资料</span>
                    </button>
                    <button class="user-dropdown-item" onclick="handleDropdownAction('password')">
                        <i class="fas fa-key"></i>
                        <span>修改密码</span>
                    </button>
                    <button class="user-dropdown-item" onclick="handleDropdownAction('settings')">
                        <i class="fas fa-cog"></i>
                        <span>账户设置</span>
                    </button>
                    <div class="user-dropdown-divider"></div>
                    <button class="user-dropdown-item danger" onclick="handleDropdownAction('logout')">
                        <i class="fas fa-sign-out-alt"></i>
                        <span>退出登录</span>
                    </button>
                </div>
            </div>
        </div>
        <div class="logout-confirm-modal" id="logoutConfirmModal">
            <div class="logout-confirm-box">
                <div class="logout-confirm-icon">
                    <i class="fas fa-exclamation-triangle"></i>
                </div>
                <div class="logout-confirm-title">确认退出登录？</div>
                <div class="logout-confirm-desc">退出后需要重新登录才能访问管理后台，确定要退出吗？</div>
                <div class="logout-confirm-btns">
                    <button class="logout-cancel-btn" onclick="closeLogoutConfirm()">取消</button>
                    <button class="logout-confirm-btn" onclick="confirmLogout()">确认退出</button>
                </div>
            </div>
        </div>
    `;

    initUserDropdown();
}

function initUserDropdown() {
    const userInfoBtn = document.getElementById('userInfoBtn');
    const dropdown = document.getElementById('userDropdown');
    if (!userInfoBtn || !dropdown) return;

    userInfoBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        dropdown.classList.toggle('show');
    });

    document.addEventListener('click', function(e) {
        if (!userInfoBtn.contains(e.target)) {
            dropdown.classList.remove('show');
        }
    });
}

function handleDropdownAction(action) {
    const dropdown = document.getElementById('userDropdown');
    dropdown.classList.remove('show');
    
    switch(action) {
        case 'profile':
            window.location.href = 'profile.html';
            break;
        case 'password':
            window.location.href = 'password.html';
            break;
        case 'settings':
            window.location.href = 'account.html';
            break;
        case 'logout':
            showLogoutConfirm();
            break;
    }
}

function showLogoutConfirm() {
    const modal = document.getElementById('logoutConfirmModal');
    if (modal) modal.classList.add('show');
}

function closeLogoutConfirm() {
    const modal = document.getElementById('logoutConfirmModal');
    if (modal) modal.classList.remove('show');
}

function confirmLogout() {
    localStorage.removeItem('admin_logged_in');
    sessionStorage.removeItem('admin_logged_in');
    window.location.href = '../admin-login.html';
}

function checkLogin() {
    const isLoggedIn = sessionStorage.getItem('admin_logged_in') || localStorage.getItem('admin_logged_in');
    if (!isLoggedIn) {
        window.location.href = '../admin-login.html';
        return false;
    }
    return true;
}

function initPageLayout() {
    if (!checkLogin()) return;
    const currentPage = getCurrentPage();
    initSidebar(currentPage);
    initTopbar(currentPage);
}

function formatNumber(num) {
    if (num >= 10000) {
        return (num / 10000).toFixed(1) + '万';
    }
    return num.toLocaleString();
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.getFullYear() + '-' + 
           String(date.getMonth() + 1).padStart(2, '0') + '-' + 
           String(date.getDate()).padStart(2, '0') + ' ' +
           String(date.getHours()).padStart(2, '0') + ':' +
           String(date.getMinutes()).padStart(2, '0');
}

function getStatusBadge(status) {
    const statusMap = {
        'pending': { text: '待处理', class: 'warning' },
        'processing': { text: '进行中', class: 'info' },
        'completed': { text: '已完成', class: 'success' },
        'cancelled': { text: '已取消', class: 'danger' },
        'rejected': { text: '已拒绝', class: 'danger' },
        'approved': { text: '已通过', class: 'success' },
        'expired': { text: '已过期', class: 'default' },
        'online': { text: '在线', class: 'success' },
        'offline': { text: '离线', class: 'default' },
        'active': { text: '活跃', class: 'success' },
        'frozen': { text: '已冻结', class: 'danger' }
    };
    const s = statusMap[status] || { text: status, class: 'default' };
    return `<span class="status-badge ${s.class}">${s.text}</span>`;
}

function generateChartSVG(data, colors) {
    const max = Math.max(...data.map(d => d.value));
    const width = 500;
    const height = 200;
    const padding = 30;
    const chartWidth = width - padding * 2;
    const chartHeight = height - padding * 2;
    const barWidth = chartWidth / data.length * 0.7;
    const barGap = chartWidth / data.length * 0.3;
    
    let svg = `<svg viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">`;
    
    // Grid lines
    for (let i = 0; i <= 4; i++) {
        const y = padding + (chartHeight / 4) * i;
        svg += `<line x1="${padding}" y1="${y}" x2="${width - padding}" y2="${y}" stroke="#E5E7EB" stroke-width="1"/>`;
    }
    
    // Bars
    data.forEach((d, i) => {
        const barHeight = (d.value / max) * chartHeight;
        const x = padding + (chartWidth / data.length) * i + barGap / 2;
        const y = height - padding - barHeight;
        const color = colors[i % colors.length];
        
        svg += `
            <rect x="${x}" y="${y}" width="${barWidth}" height="${barHeight}" rx="4" fill="${color}" opacity="0.9"/>
            <text x="${x + barWidth / 2}" y="${y - 5}" text-anchor="middle" font-size="11" fill="#6B7280">${d.value}</text>
            <text x="${x + barWidth / 2}" y="${height - padding + 18}" text-anchor="middle" font-size="11" fill="#9CA3AF">${d.label}</text>
        `;
    });
    
    svg += '</svg>';
    return svg;
}

function generateLineChartSVG(data, colors) {
    const max = Math.max(...data.flat().map(d => d.value));
    const width = 500;
    const height = 200;
    const padding = 30;
    const chartWidth = width - padding * 2;
    const chartHeight = height - padding * 2;
    
    let svg = `<svg viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">`;
    
    // Grid lines
    for (let i = 0; i <= 4; i++) {
        const y = padding + (chartHeight / 4) * i;
        svg += `<line x1="${padding}" y1="${y}" x2="${width - padding}" y2="${y}" stroke="#E5E7EB" stroke-width="1"/>`;
    }
    
    // X-axis labels
    const labels = data[0].map(d => d.label);
    labels.forEach((label, i) => {
        const x = padding + (chartWidth / (labels.length - 1)) * i;
        svg += `<text x="${x}" y="${height - padding + 18}" text-anchor="middle" font-size="11" fill="#9CA3AF">${label}</text>`;
    });
    
    // Lines
    data.forEach((series, sIdx) => {
        const color = colors[sIdx];
        let path = '';
        series.forEach((point, i) => {
            const x = padding + (chartWidth / (series.length - 1)) * i;
            const y = height - padding - (point.value / max) * chartHeight;
            path += i === 0 ? `M ${x} ${y}` : ` L ${x} ${y}`;
        });
        svg += `<path d="${path}" fill="none" stroke="${color}" stroke-width="2"/>`;
        
        series.forEach((point, i) => {
            const x = padding + (chartWidth / (series.length - 1)) * i;
            const y = height - padding - (point.value / max) * chartHeight;
            svg += `<circle cx="${x}" cy="${y}" r="4" fill="${color}"/>`;
        });
    });
    
    svg += '</svg>';
    return svg;
}

function generateDonutChartSVG(percentages, colors, size = 160) {
    const radius = size / 2 - 10;
    const cx = size / 2;
    const cy = size / 2;
    const circumference = 2 * Math.PI * radius;
    
    let svg = `<svg viewBox="0 0 ${size} ${size}" xmlns="http://www.w3.org/2000/svg">`;
    svg += `<circle cx="${cx}" cy="${cy}" r="${radius}" fill="none" stroke="#F3F4F6" stroke-width="20"/>`;
    
    let offset = 0;
    percentages.forEach((p, i) => {
        const dashLength = (p / 100) * circumference;
        svg += `
            <circle cx="${cx}" cy="${cy}" r="${radius}" fill="none" 
                    stroke="${colors[i]}" stroke-width="20"
                    stroke-dasharray="${dashLength} ${circumference}"
                    stroke-dashoffset="${-offset}"
                    transform="rotate(-90 ${cx} ${cy})"
                    stroke-linecap="butt"/>
        `;
        offset += dashLength;
    });
    
    svg += '</svg>';
    return svg;
}

function generateChromeFrame(src, label) {
    return `
        <div class="chrome-frame">
            <div class="chrome-header">
                <div class="chrome-dots">
                    <div class="chrome-dot red"></div>
                    <div class="chrome-dot yellow"></div>
                    <div class="chrome-dot green"></div>
                </div>
                <div class="chrome-address">
                    <i class="fas fa-lock" style="color:#28C840;"></i>
                    <span>admin.kuaima.com/${label || ''}</span>
                </div>
            </div>
            <div class="chrome-content">
                <iframe src="${src}"></iframe>
            </div>
        </div>
    `;
}