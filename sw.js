/**
 * 求职防骗助手 - Service Worker
 * 支持离线使用和 PWA 安装
 */

const CACHE_NAME = 'job-analyzer-v1';

// 需要缓存的文件列表
const urlsToCache = [
    'index.html',
    'styles.css',
    'app.js',
    'manifest.json',
    'icons/icon.svg'
];

// 安装 Service Worker
self.addEventListener('install', function(event) {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(function(cache) {
                console.log('缓存已打开');
                return cache.addAll(urlsToCache);
            })
    );
    // 立即激活
    self.skipWaiting();
});

// 激活 Service Worker
self.addEventListener('activate', function(event) {
    event.waitUntil(
        caches.keys().then(function(cacheNames) {
            return Promise.all(
                cacheNames.map(function(cacheName) {
                    if (cacheName !== CACHE_NAME) {
                        console.log('删除旧缓存:', cacheName);
                        return caches.delete(cacheName);
                    }
                })
            );
        })
    );
    // 立即接管所有页面
    self.clients.claim();
});

// 拦截网络请求
self.addEventListener('fetch', function(event) {
    event.respondWith(
        caches.match(event.request)
            .then(function(response) {
                // 如果有缓存则返回缓存，否则发起网络请求
                if (response) {
                    return response;
                }
                return fetch(event.request).then(function(networkResponse) {
                    // 只缓存同源的有效响应
                    if (!networkResponse || networkResponse.status !== 200 || networkResponse.type !== 'basic') {
                        return networkResponse;
                    }
                    const responseToCache = networkResponse.clone();
                    caches.open(CACHE_NAME).then(function(cache) {
                        cache.put(event.request, responseToCache);
                    });
                    return networkResponse;
                }).catch(function() {
                    // 离线时返回备用页面
                    return caches.match('index.html');
                });
            })
    );
});
