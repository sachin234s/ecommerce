<script setup>
import { computed, onMounted, ref, watch } from 'vue'

const activeView = ref('Overview')
const region = ref('India')
const search = ref('')
const toast = ref('')
const noticeCount = ref(3)
const apiError = ref('')
const summary = ref(null)
const showCreateDialog = ref(false)
const formError = ref('')
const form = ref({})

const navItems = [
  { name: 'Overview', icon: '◫' },
  { name: 'Orders', icon: '▤' },
  { name: 'Inventory', icon: '▧' },
  { name: 'Customers', icon: '♙' },
  { name: 'Payments', icon: '▣' },
]

const orders = ref([])
const products = ref([])
const customers = ref([])
const payments = ref([])
const colors = ['lavender', 'peach', 'mint', 'blue', 'rose']

async function requestApi(path, options) {
  const response = await fetch(`/api/v1/${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options?.headers },
  })
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.detail || body?.message || `Request failed (${response.status})`)
  }
  return response.status === 204 ? null : response.json()
}

function initials(name) {
  return name.split(/\s+/).slice(0, 2).map((part) => part[0]).join('').toUpperCase()
}

function formattedDate(value) {
  return new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

function mapOrders(rows) {
  return rows.map((row) => ({
    id: `ORD-${String(row.id).padStart(5, '0')}`,
    customer: row.customerReference || row.customer, initials: initials(row.customerReference || row.customer), color: colors[row.id % colors.length],
    date: formattedDate(row.createdAt), amount: row.totalMinor / 100,
    method: row.paymentMethod, status: row.status, items: `${row.itemCount} items`,
  }))
}

function mapProducts(rows) {
  return rows.map((row) => ({
    id: `SKU-${String(row.id).padStart(4, '0')}`, customer: row.name,
    initials: initials(row.name), color: colors[row.id % colors.length],
    date: `${row.category} · ${row.variant}`, amount: row.unitPriceMinor / 100,
    method: `Warehouse · ${row.warehouse}`, status: (row.available ?? row.stock) < 10 ? 'Low stock' : 'In stock',
    items: `${row.available ?? row.stock} units`,
  }))
}

function mapCustomers(rows) {
  return rows.map((row) => ({
    id: `CUS-${String(row.id).padStart(4, '0')}`, customer: row.name,
    initials: initials(row.name), color: colors[row.id % colors.length],
    date: `${row.city} · ${row.email}`, amount: row.lifetimeValueMinor / 100,
    method: `${row.preferredPayment} preferred`, status: row.vip ? 'VIP' : 'Active',
    items: `${row.orderCount} orders`,
  }))
}

function mapPayments(rows) {
  return rows.map((row) => ({
    id: `PAY-${String(row.id).padStart(5, '0')}`, customer: row.customer || row.orderReference,
    initials: initials(row.customer || row.orderReference), color: colors[row.id % colors.length],
    date: formattedDate(row.createdAt), amount: row.amountMinor / 100,
    method: row.method, status: row.status, items: row.orderReference,
  }))
}

async function loadView() {
  if (activeView.value === 'Architecture') return
  apiError.value = ''
  try {
    if (activeView.value === 'Overview') {
      const [result, orderRows] = await Promise.all([requestApi('dashboard/summary'), requestApi('orders')])
      summary.value = result
      orders.value = mapOrders(orderRows)
      return
    }
    const routes = { Orders: 'orders', Inventory: 'inventory', Customers: 'customers', Payments: 'payments' }
    const rows = await requestApi(routes[activeView.value])
    if (activeView.value === 'Orders') orders.value = mapOrders(rows)
    if (activeView.value === 'Inventory') products.value = mapProducts(rows)
    if (activeView.value === 'Customers') customers.value = mapCustomers(rows)
    if (activeView.value === 'Payments') payments.value = mapPayments(rows)
  } catch (error) {
    apiError.value = `Could not connect to the commerce API: ${error.message}`
  }
}

onMounted(loadView)
watch(activeView, loadView)

const currentRows = computed(() => {
  const rows = activeView.value === 'Inventory' ? products.value
    : activeView.value === 'Customers' ? customers.value
      : activeView.value === 'Payments' ? payments.value : orders.value
  const term = search.value.trim().toLowerCase()
  return term ? rows.filter((row) => Object.values(row).some((value) => String(value).toLowerCase().includes(term))) : rows
})

const currency = computed(() => region.value === 'India' ? 'INR' : 'USD')
const locale = computed(() => region.value === 'India' ? 'en-IN' : 'en-US')
const marketId = computed(() => region.value === 'India' ? 'IN' : 'US')

function money(amount) {
  return new Intl.NumberFormat(locale.value, {
    style: 'currency',
    currency: currency.value,
    maximumFractionDigits: 0,
  }).format(region.value === 'India' ? amount : amount / 83)
}

function notify(message) {
  toast.value = message
  window.clearTimeout(notify.timeout)
  notify.timeout = window.setTimeout(() => { toast.value = '' }, 2600)
}

function createRecord() {
  if (activeView.value === 'Overview') activeView.value = 'Orders'
  formError.value = ''
  form.value = {}
  showCreateDialog.value = true
}

const createFields = computed(() => {
  const fields = {
    Orders: [
      { key: 'customer', label: 'Customer name', required: true },
      { key: 'paymentMethod', label: 'Payment method', required: true, options: region.value === 'India' ? ['UPI', 'CARD', 'NETBANKING', 'COD'] : ['CARD', 'WALLET'] },
      { key: 'total', label: `Order total (${currency.value})`, required: true, type: 'number', min: 1 },
      { key: 'itemCount', label: 'Number of items', required: true, type: 'number', min: 1 },
    ],
    Inventory: [
      { key: 'name', label: 'Product name', required: true },
      { key: 'category', label: 'Category', required: true },
      { key: 'variant', label: 'Variant', required: true },
      { key: 'warehouse', label: 'Warehouse city', required: true },
      { key: 'price', label: `Unit price (${currency.value})`, required: true, type: 'number', min: 0 },
      { key: 'stock', label: 'Starting stock', required: true, type: 'number', min: 0 },
    ],
    Customers: [
      { key: 'name', label: 'Full name', required: true },
      { key: 'email', label: 'Email', required: true, type: 'email' },
      { key: 'city', label: 'City', required: true },
      { key: 'preferredPayment', label: 'Preferred payment', required: true, options: region.value === 'India' ? ['UPI', 'CARD', 'NETBANKING', 'COD'] : ['CARD', 'WALLET'] },
    ],
    Payments: [
      { key: 'orderReference', label: 'Order reference', required: true },
      { key: 'customer', label: 'Customer name', required: true },
      { key: 'method', label: 'Sandbox payment method', required: true, options: region.value === 'India' ? ['UPI sandbox', 'Card sandbox', 'Netbanking sandbox', 'COD'] : ['Card sandbox', 'Wallet sandbox'] },
      { key: 'amount', label: `Payment amount (${currency.value})`, required: true, type: 'number', min: 1 },
    ],
  }
  return fields[activeView.value] || fields.Orders
})

async function submitRecord() {
  formError.value = ''
  const values = form.value
  const requests = {
    Orders: {
      path: 'orders',
      body: { customerReference: values.customer, market: marketId.value, currency: currency.value, paymentMethod: values.paymentMethod, totalMinor: Math.round(Number(values.total) * 100), itemCount: Number(values.itemCount) },
      message: 'Order created',
    },
    Inventory: {
      path: 'inventory',
      body: { name: values.name, category: values.category, variant: values.variant, warehouse: values.warehouse, market: marketId.value, currency: currency.value, unitPriceMinor: Math.round(Number(values.price) * 100), stock: Number(values.stock) },
      message: 'Product added to inventory',
    },
    Customers: {
      path: 'customers',
      body: { name: values.name, email: values.email, market: region.value === 'India' ? 'IN' : 'US', city: values.city, preferredPayment: values.preferredPayment, marketingConsent: false },
      message: 'Customer created',
    },
    Payments: {
      path: 'payments/intents',
      body: { orderReference: values.orderReference, market: marketId.value, currency: currency.value, method: values.method, amountMinor: Math.round(Number(values.amount) * 100) },
      message: 'Sandbox payment recorded as pending',
    },
  }
  const request = requests[activeView.value]
  try {
    await requestApi(request.path, { method: 'POST', body: JSON.stringify(request.body) })
    showCreateDialog.value = false
    notify(request.message)
    await loadView()
  } catch (error) {
    formError.value = error.message
  }
}

const pageSubtitle = computed(() => ({
  Overview: 'Here’s what’s happening with your store today.',
  Orders: 'Track, manage, and fulfil customer orders.',
  Inventory: 'Keep products, stock levels, and warehouses in sync.',
  Customers: 'Get to know the people behind every order.',
  Payments: 'Monitor collections, refunds, and settlement health.',
}[activeView.value]))

const primaryAction = computed(() => ({
  Overview: 'Create order',
  Orders: 'Create order',
  Inventory: 'Add product',
  Customers: 'Add customer',
  Payments: 'Record payment',
}[activeView.value]))
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <a class="brand" href="#" aria-label="Morrow Commerce home" @click.prevent="activeView = 'Overview'">
        <span class="brand-symbol"><span></span><span></span><span></span><span></span></span>
        <span class="brand-name">morrow<span>.</span></span>
      </a>

      <div class="workspace-switcher">
        <div class="workspace-logo">M</div>
        <div class="workspace-copy"><strong>Morrow Studio</strong><span>Commerce workspace</span></div>
        <span class="switcher-chevron">⌄</span>
      </div>

      <p class="nav-label">WORKSPACE</p>
      <nav class="main-nav" aria-label="Main navigation">
        <button v-for="item in navItems" :key="item.name" class="nav-item" :class="{ active: activeView === item.name }" @click="activeView = item.name">
          <span class="nav-icon">{{ item.icon }}</span><span>{{ item.name }}</span><span v-if="item.count" class="nav-count">{{ item.count }}</span>
        </button>
      </nav>

      <p class="nav-label tools-label">TOOLS</p>
      <button class="nav-item" :class="{ active: activeView === 'Architecture' }" @click="activeView = 'Architecture'">
        <span class="nav-icon">⌘</span><span>Architecture</span><span class="new-badge">NEW</span>
      </button>

      <div class="sidebar-bottom">
        <div class="help-card"><div class="help-icon">✳</div><strong>Need a hand?</strong><p>Our team is here to help you grow.</p><button @click="notify('Support request started')">Visit help centre <span>↗</span></button></div>
        <button class="profile-button" @click="notify('Account settings opened')">
          <span class="profile-avatar">SK</span><span class="profile-copy"><strong>Sana Kapoor</strong><small>Store owner</small></span><span class="profile-menu">···</span>
        </button>
      </div>
    </aside>

    <main class="main-area">
      <header class="topbar">
        <div class="breadcrumbs"><span>Workspace</span><span class="breadcrumb-slash">/</span><strong>{{ activeView }}</strong></div>
        <div class="topbar-actions">
          <label class="region-control"><span class="region-flag">{{ region === 'India' ? 'IN' : 'GL' }}</span><select v-model="region" aria-label="Store region"><option>India</option><option>Global</option></select><span class="select-chevron">⌄</span></label>
          <button class="icon-button" aria-label="Help" @click="notify('Help centre opened')">?</button>
          <button class="icon-button notification-button" aria-label="Notifications" @click="noticeCount = 0; notify('You’re all caught up')">♧<span v-if="noticeCount" class="notification-dot"></span></button>
          <span class="topbar-divider"></span>
          <button class="top-avatar" aria-label="Open profile">SK</button>
        </div>
      </header>

      <div class="content">
        <div v-if="apiError" class="api-error" role="alert">
          <span>{{ apiError }}</span><button @click="loadView">Retry</button>
        </div>
        <section class="page-heading">
          <div><p class="date-line">THURSDAY, OCTOBER 29, 2026 <span class="date-spark">✳</span></p><h1>{{ activeView === 'Architecture' ? 'Built to scale with you.' : activeView }}</h1><p class="page-subtitle">{{ activeView === 'Architecture' ? 'A modular commerce platform, ready for every market.' : pageSubtitle }}</p></div>
          <div class="heading-actions" v-if="activeView !== 'Architecture'">
            <button class="secondary-button" @click="notify('Report is being prepared')"><span>↓</span> Export</button>
            <button class="primary-button" @click="createRecord"><span>＋</span> {{ primaryAction }}</button>
          </div>
        </section>

        <template v-if="activeView === 'Overview'">
          <section class="metrics-grid" aria-label="Store performance">
            <article class="metric-card"><div class="metric-top"><span>Gross sales <span class="info-dot">i</span></span><span class="metric-icon purple">↗</span></div><strong>{{ money((summary?.salesMinor || 0) / 100) }}</strong><div class="metric-foot"><span class="trend positive">Live data</span><span>captured orders</span><svg class="sparkline purple-line" viewBox="0 0 86 28" aria-hidden="true"><path d="M1 22 C12 20 12 17 20 19 S31 12 39 15 S48 13 54 16 S66 4 72 9 S80 5 85 2"/></svg></div></article>
            <article class="metric-card"><div class="metric-top"><span>Orders <span class="info-dot">i</span></span><span class="metric-icon orange">▤</span></div><strong>{{ summary?.orderCount || 0 }}</strong><div class="metric-foot"><span class="trend positive">Live data</span><span>all orders</span><svg class="sparkline orange-line" viewBox="0 0 86 28" aria-hidden="true"><path d="M1 23 C10 19 14 22 21 17 S32 20 41 13 S49 15 56 10 S66 14 73 7 S80 7 85 2"/></svg></div></article>
            <article class="metric-card"><div class="metric-top"><span>Customers <span class="info-dot">i</span></span><span class="metric-icon green">♙</span></div><strong>{{ summary?.customerCount || 0 }}</strong><div class="metric-foot"><span class="trend positive">Live data</span><span>registered customers</span><svg class="sparkline green-line" viewBox="0 0 86 28" aria-hidden="true"><path d="M1 5 C12 8 15 4 23 10 S34 7 42 13 S49 11 57 16 S66 11 72 18 S80 17 85 24"/></svg></div></article>
            <article class="metric-card"><div class="metric-top"><span>Payment records <span class="info-dot">i</span></span><span class="metric-icon blue">✓</span></div><strong>{{ summary?.paymentCount || 0 }}</strong><div class="metric-foot"><span class="trend positive">Live data</span><span>{{ summary?.inventoryAlerts || 0 }} stock alerts</span><svg class="sparkline blue-line" viewBox="0 0 86 28" aria-hidden="true"><path d="M1 21 C11 18 14 20 21 13 S34 17 42 10 S51 12 59 8 S67 9 73 4 S81 7 85 2"/></svg></div></article>
          </section>

          <section class="overview-grid">
            <article class="panel sales-panel">
              <div class="panel-heading"><div><h2>Sales overview</h2><p>Captured payments across the demo store.</p></div><span class="period-select">All time</span></div>
              <div class="chart-summary"><strong>{{ money((summary?.salesMinor || 0) / 100) }}</strong><span class="trend positive">Live</span><small>captured order total</small></div>
              <div class="chart-wrap">
                <div class="y-labels"><span>{{ region === 'India' ? '₹' : '$' }} 60k</span><span>{{ region === 'India' ? '₹' : '$' }} 40k</span><span>{{ region === 'India' ? '₹' : '$' }} 20k</span><span>{{ region === 'India' ? '₹' : '$' }} 0</span></div>
                <svg class="sales-chart" viewBox="0 0 700 190" preserveAspectRatio="none" role="img" aria-label="Sales trend chart for the last seven days">
                  <defs><linearGradient id="salesFill" x1="0" x2="0" y1="0" y2="1"><stop offset="0" stop-color="#7458e8" stop-opacity=".18"/><stop offset="1" stop-color="#7458e8" stop-opacity="0"/></linearGradient></defs>
                  <path class="grid-line" d="M0 22H700M0 69H700M0 116H700M0 163H700"/>
                  <path class="chart-area" d="M0 130 C27 119 42 128 70 107 S116 100 140 112 S186 86 210 94 S254 67 280 83 S327 101 350 73 S397 68 420 75 S465 36 490 49 S535 68 560 45 S605 37 630 48 S675 17 700 23 V163 H0Z"/>
                  <path class="chart-line" d="M0 130 C27 119 42 128 70 107 S116 100 140 112 S186 86 210 94 S254 67 280 83 S327 101 350 73 S397 68 420 75 S465 36 490 49 S535 68 560 45 S605 37 630 48 S675 17 700 23"/>
                  <circle cx="700" cy="23" r="5" class="chart-dot"/>
                </svg>
                <div class="x-labels"><span>Oct 23</span><span>Oct 24</span><span>Oct 25</span><span>Oct 26</span><span>Oct 27</span><span>Oct 28</span><span>Oct 29</span></div>
              </div>
            </article>
            <article class="panel channels-panel">
              <div class="panel-heading"><div><h2>Sales by channel</h2><p>Illustrative channel mix.</p></div><button class="more-button" aria-label="More channel options" @click="notify('Channel report opened')">···</button></div>
              <div class="donut-wrap"><div class="donut"><div class="donut-center"><strong>{{ money((summary?.salesMinor || 0) / 100) }}</strong><span>Captured sales</span></div></div></div>
              <div class="channel-legend">
                <div><span class="legend-name"><i class="legend-dot web"></i>Online store</span><strong>62%</strong></div>
                <div><span class="legend-name"><i class="legend-dot social"></i>Social commerce</span><strong>24%</strong></div>
                <div><span class="legend-name"><i class="legend-dot marketplace"></i>Marketplaces</span><strong>14%</strong></div>
              </div>
            </article>
          </section>
        </template>

        <section v-if="activeView !== 'Architecture'" class="panel table-panel">
          <div class="table-toolbar">
            <div><h2>{{ activeView === 'Overview' ? 'Recent orders' : activeView === 'Inventory' ? 'Product catalogue' : activeView === 'Customers' ? 'Customer directory' : `${activeView} activity` }}</h2><p>{{ activeView === 'Overview' ? 'The latest activity from your store.' : `${currentRows.length} records · Updated just now` }}</p></div>
            <div class="table-actions"><label class="search-box"><span>⌕</span><input v-model="search" placeholder="Search..." aria-label="Search records"><kbd>⌘ K</kbd></label><button class="filter-button" @click="notify('Filters are up to date')">☷ <span>Filter</span><i></i></button><button class="view-all" v-if="activeView === 'Overview'" @click="activeView = 'Orders'">View all <span>→</span></button></div>
          </div>
          <div class="table-scroll"><table><thead><tr><th>{{ activeView === 'Inventory' ? 'PRODUCT' : activeView === 'Customers' ? 'CUSTOMER' : 'REFERENCE' }}</th><th>{{ activeView === 'Inventory' ? 'CATEGORY' : activeView === 'Customers' ? 'LOCATION' : activeView === 'Payments' ? 'PAYMENT METHOD' : 'CUSTOMER' }}</th><th>{{ activeView === 'Inventory' ? 'LOCATION' : activeView === 'Customers' ? 'LIFETIME VALUE' : 'DATE' }}</th><th>{{ activeView === 'Inventory' ? 'PRICE' : activeView === 'Customers' ? 'PREFERENCE' : 'AMOUNT' }}</th><th>STATUS</th><th>{{ activeView === 'Inventory' ? 'UNITS' : activeView === 'Customers' ? 'ORDERS' : activeView === 'Payments' ? 'ORDER' : 'PAYMENT' }}</th><th></th></tr></thead>
            <tbody><tr v-for="row in currentRows" :key="row.id"><td><div class="record-cell"><span class="record-avatar" :class="row.color">{{ row.initials }}</span><span><strong>{{ row.id }}</strong><small>{{ row.customer }}</small></span></div></td><td><span v-if="activeView === 'Inventory'">{{ row.date }}</span><div v-else-if="activeView === 'Customers'" class="date-cell">{{ row.date }}</div><span v-else-if="activeView === 'Payments'">{{ row.method }}</span><div v-else class="record-cell customer-cell"><span class="record-avatar tiny" :class="row.color">{{ row.initials }}</span>{{ row.customer }}</div></td><td>{{ activeView === 'Inventory' ? row.method : activeView === 'Customers' ? money(row.amount) : row.date }}</td><td class="amount-cell">{{ activeView === 'Customers' ? row.method : money(row.amount) }}</td><td><span class="status-pill" :class="row.status.toLowerCase().replaceAll(' ', '-')"><i></i>{{ row.status }}</span></td><td>{{ row.items }}</td><td><button class="row-menu" :aria-label="`Actions for ${row.id}`" @click="notify(`${row.id} details opened`)">···</button></td></tr>
              <tr v-if="currentRows.length === 0"><td colspan="7" class="empty-state">No records match “{{ search }}”. Try another search.</td></tr>
            </tbody></table></div>
          <div class="table-footer"><span>Showing <strong>{{ currentRows.length }}</strong> of <strong>{{ activeView === 'Overview' ? '1,284' : activeView === 'Inventory' ? '248' : activeView === 'Customers' ? '8,492' : '1,284' }}</strong> {{ activeView.toLowerCase() }}</span><div class="pagination"><button disabled aria-label="Previous page">←</button><button class="page-current">1</button><button @click="notify('Page 2 selected')">2</button><button @click="notify('Page 3 selected')">3</button><span>…</span><button @click="notify('Last page selected')">12</button><button aria-label="Next page" @click="notify('Next page selected')">→</button></div></div>
        </section>

        <template v-if="activeView === 'Architecture'">
              <section class="architecture-banner"><div class="architecture-glow"></div><span class="architecture-tag"><i></i> INDEPENDENT JAVA MICROSERVICES</span><h2>One commerce core.<br><em>Every market.</em></h2><p>Tomcat REST services, a Spring Cloud gateway, and isolated PostgreSQL databases keep every domain independently deployable.</p><button class="architecture-cta" @click="notify('See README for API run instructions')">Explore the platform <span>→</span></button><div class="banner-orbit orbit-one"></div><div class="banner-orbit orbit-two"></div><div class="banner-core">m<span>.</span></div></section>
          <section class="architecture-flow">
            <div class="architecture-column"><div class="flow-label">EXPERIENCE</div><article class="arch-card"><span class="arch-symbol violet">⌘</span><div><strong>Storefront & Admin</strong><small>Vue · REST APIs</small></div><span class="healthy-dot"></span></article><article class="arch-card"><span class="arch-symbol dark">⇄</span><div><strong>Spring Cloud Gateway</strong><small>JWT · routing · port 8080</small></div><span class="healthy-dot"></span></article></div>
            <div class="flow-connector"><span></span><span></span><span></span></div>
            <div class="architecture-column service-column"><div class="flow-label">JAVA MICROSERVICES <span>SPRING BOOT · TOMCAT</span></div><div class="service-grid"><article class="arch-card"><span class="arch-symbol violet">♙</span><div><strong>Customer</strong><small>Profile · consent · REST</small></div></article><article class="arch-card"><span class="arch-symbol orange">▤</span><div><strong>Order</strong><small>Lifecycle · REST</small></div></article><article class="arch-card"><span class="arch-symbol green">▧</span><div><strong>Inventory</strong><small>Stock · adjustments · REST</small></div></article><article class="arch-card"><span class="arch-symbol blue">₹</span><div><strong>Payment</strong><small>Idempotent sandbox intents</small></div></article><article class="arch-card"><span class="arch-symbol pink">⌁</span><div><strong>Market</strong><small>Currency · locale · methods</small></div></article><article class="arch-card"><span class="arch-symbol yellow">↗</span><div><strong>Dashboard</strong><small>Cross-service read model</small></div></article></div></div>
            <div class="flow-connector"><span></span><span></span><span></span></div>
            <div class="architecture-column data-column"><div class="flow-label">DATA & EVENTS</div><article class="arch-card"><span class="arch-symbol blue">▤</span><div><strong>PostgreSQL</strong><small>Separate database per service</small></div></article><article class="arch-card"><span class="arch-symbol orange">⌁</span><div><strong>Apache Kafka</strong><small>Next step: async domain events</small></div></article><article class="arch-card"><span class="arch-symbol violet">⌕</span><div><strong>Redis + OpenSearch</strong><small>Scale-out cache and catalog search</small></div></article></div>
          </section>
          <section class="market-adapters"><div><span class="adapters-icon">◎</span><div><strong>Swap the market adapter, not the platform.</strong><p>Country packs isolate local payment rails, tax rules, address formats, and compliance from the commerce core.</p></div></div><div class="adapter-tags"><span class="market-tag active-market">🇮🇳 India <small>UPI · GST · COD</small></span><span class="market-tag">🌐 Global <small>Cards · VAT · locales</small></span><span class="adapter-plus">＋</span></div></section>
          <section class="architecture-footnote"><span>Six independently runnable Spring Boot services; payment integration and event streaming are sandbox/planned.</span><span>SPRING BOOT <i>·</i> JAVA 17 <i>·</i> MAVEN <i>·</i> POSTGRESQL</span></section>
        </template>

        <footer class="app-footer"><span>© 2026 Morrow Commerce</span><span><i class="footer-status" :class="{ offline: apiError }"></i> {{ apiError ? 'API disconnected' : 'API connected' }} <b>·</b> v0.1.0</span><a href="#" @click.prevent="activeView = 'Architecture'">Platform architecture ↗</a></footer>
      </div>
    </main>
    <div v-if="showCreateDialog" class="modal-backdrop" @click.self="showCreateDialog = false">
      <form class="create-dialog" @submit.prevent="submitRecord">
        <button type="button" class="modal-close" aria-label="Close dialog" @click="showCreateDialog = false">×</button>
        <span class="modal-eyebrow">MORROW COMMERCE</span>
        <h2>{{ primaryAction }}</h2>
        <p class="modal-description">{{ activeView === 'Payments' ? 'Creates a pending sandbox record only. No payment is collected.' : 'This record will be saved to the commerce API.' }}</p>
        <label v-for="field in createFields" :key="field.key" class="form-field">
          <span>{{ field.label }}</span>
          <select v-if="field.options" v-model="form[field.key]" :required="field.required">
            <option value="" disabled>Select an option</option>
            <option v-for="option in field.options" :key="option" :value="option">{{ option }}</option>
          </select>
          <input v-else v-model="form[field.key]" :type="field.type || 'text'" :required="field.required" :min="field.min" :step="field.type === 'number' ? 'any' : undefined">
        </label>
        <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
        <div class="modal-actions"><button type="button" class="secondary-button" @click="showCreateDialog = false">Cancel</button><button type="submit" class="primary-button">Save record</button></div>
      </form>
    </div>
    <Transition name="toast"><div v-if="toast" class="toast-message"><span>✓</span>{{ toast }}</div></Transition>
  </div>
</template>
