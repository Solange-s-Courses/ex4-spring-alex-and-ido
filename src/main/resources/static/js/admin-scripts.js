/* Clean admin-scripts.js - Truly Minimal */

// Chart instances and data
let charts = { tab1: null, tab2: null, tab3: null };
let metricsData = {};

// Chart configurations
const chartConfigs = {
    tab1: {
        url: '/admin/metrics/user-roles',
        chartId: 'userRoleChart',
        dataKey: 'roleCounts',
        totalKey: 'totalUsers',
        legends: [
            { id: 'chiefCount', key: 'chief', label: 'Chief', color: '#007bff' },
            { id: 'managerCount', key: 'manager', label: 'Manager', color: '#28a745' },
            { id: 'userCount', key: 'user', label: 'User', color: '#fd7e14' }
        ],
        totalId: 'totalUserCount',
        emptyMessage: 'No users in system'
    },
    tab2: {
        url: '/admin/metrics/item-status',
        chartId: 'itemStatusChart',
        dataKey: 'statusCounts',
        totalKey: 'totalItems',
        legends: [
            { id: 'availableCount', key: 'available', label: 'Available', color: '#28a745' },
            { id: 'inUseCount', key: 'inUse', label: 'In Use', color: '#ffc107' },
            { id: 'unavailableCount', key: 'unavailable', label: 'Unavailable', color: '#dc3545' }
        ],
        totalId: 'totalItemCount',
        emptyMessage: 'No items in system'
    },
    tab3: {
        url: '/admin/metrics/event-status',
        chartId: 'eventStatusChart',
        dataKey: 'statusCounts',
        totalKey: 'totalEvents',
        legends: [
            { id: 'notActiveCount', key: 'notActive', label: 'Not Active', color: '#6c757d' },
            { id: 'activeCount', key: 'active', label: 'Active', color: '#007bff' },
            { id: 'equipmentReturnCount', key: 'equipmentReturn', label: 'Equipment Return', color: '#17a2b8' }
        ],
        totalId: 'totalEventCount',
        emptyMessage: 'No events in system'
    }
};

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadAllMetricsData();
    switchTab('tab1');
});

// Load data for all metrics
async function loadAllMetricsData() {
    try {
        const promises = Object.values(chartConfigs).map(config =>
            fetch(config.url).then(r => r.json())
        );
        const [userData, itemData, eventData] = await Promise.all(promises);
        metricsData = { tab1: userData, tab2: itemData, tab3: eventData };
    } catch (error) {
        console.error('Error loading metrics:', error);
    }
}

// Create chart for active tab
function createChartForActiveTab(tabId) {
    if (!charts[tabId] && metricsData[tabId]) {
        updateChart(tabId, metricsData[tabId]);
    }
}

// Generic chart update function
function updateChart(tabId, data) {
    const config = chartConfigs[tabId];
    const counts = data[config.dataKey];
    const total = data[config.totalKey];

    // Update legends
    config.legends.forEach(legend => {
        document.getElementById(legend.id).textContent = counts[legend.key];
    });
    document.getElementById(config.totalId).textContent = total;

    // Create chart
    const ctx = document.getElementById(config.chartId).getContext('2d');
    if (charts[tabId]) charts[tabId].destroy();

    if (total === 0) {
        charts[tabId] = createEmptyChart(ctx, config.emptyMessage);
        return;
    }

    const chartData = config.legends
        .filter(legend => counts[legend.key] > 0)
        .map(legend => ({
            data: counts[legend.key],
            label: legend.label,
            color: legend.color
        }));

    charts[tabId] = createChart(ctx, chartData, total);
}

// Create pie chart
function createChart(ctx, items, total) {
    return new Chart(ctx, {
        type: 'pie',
        data: {
            labels: items.map(item => item.label),
            datasets: [{
                data: items.map(item => item.data),
                backgroundColor: items.map(item => item.color),
                borderColor: '#ffffff',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#333333',
                    titleColor: '#ffffff',
                    bodyColor: '#ffffff',
                    callbacks: {
                        label: function(context) {
                            const percentage = ((context.parsed / total) * 100).toFixed(1);
                            return `${context.label}: ${context.parsed} (${percentage}%)`;
                        }
                    }
                }
            },
            animation: {
                animateRotate: true,
                animateScale: true,
                duration: 1200,
                easing: 'easeInOutQuart'
            }
        }
    });
}

// Create empty chart
function createEmptyChart(ctx, message) {
    const chart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ['No Data'],
            datasets: [{ data: [1], backgroundColor: ['#e9ecef'], borderColor: ['#dee2e6'], borderWidth: 2 }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: { legend: { display: false }, tooltip: { enabled: false } }
        }
    });

    const wrapper = ctx.canvas.closest('.chart-wrapper');
    if (wrapper && !wrapper.querySelector('.empty-chart-message')) {
        const overlay = document.createElement('div');
        overlay.className = 'empty-chart-message';
        overlay.textContent = message;
        overlay.style.cssText = `
            position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
            color: #6c757d; font-weight: 500; pointer-events: none; text-align: center;
        `;
        wrapper.appendChild(overlay);
    }

    return chart;
}

// Switch tabs
function switchTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));

    document.getElementById(tabId).classList.add('active');
    document.querySelector(`[data-tab="${tabId}"]`).classList.add('active');

    setTimeout(() => createChartForActiveTab(tabId), 100);
}