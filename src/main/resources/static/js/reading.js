/**
 * 阅读体验相关的前端小脚本（没有用任何框架，纯原生 JS）。
 *
 * 只做两件事：
 *   ① 侧边栏「外观」里切换卡片配色：改 <html data-card="..."> 并存进 localStorage；
 *   ② 首页下滑时，文章列表淡入上浮（IntersectionObserver 监听元素是否进入视口）。
 *
 * 为什么配色要存在浏览器而不是数据库？
 * 因为它是"这台设备上的人偏好"，管理员不该替访客决定；而且这样不用加接口、
 * 不用改表，纯前端就能做到"记住选择"。
 */
(function () {
    'use strict';

    var STORAGE_KEY = 'blog.cardTheme';
    var THEMES = ['snow', 'sand', 'pine'];   // 必须和 style.css 里的 html[data-card="..."] 对应
    var root = document.documentElement;

    /** 应用某个配色：贴属性 + 高亮对应色板 + 需要时写进 localStorage */
    function applyTheme(theme, persist) {
        if (THEMES.indexOf(theme) === -1) {
            theme = 'snow';
        }
        root.setAttribute('data-card', theme);

        var buttons = document.querySelectorAll('[data-card-set]');
        for (var i = 0; i < buttons.length; i++) {
            var isCurrent = buttons[i].getAttribute('data-card-set') === theme;
            buttons[i].setAttribute('aria-pressed', isCurrent ? 'true' : 'false');
        }

        if (persist) {
            try {
                localStorage.setItem(STORAGE_KEY, theme);
            } catch (e) {
                /* 隐私模式/禁用存储：这次照样生效，只是记不住 */
            }
        }
    }

    // ---------- ① 色板点击 ----------
    var swatches = document.querySelectorAll('[data-card-set]');
    for (var i = 0; i < swatches.length; i++) {
        swatches[i].addEventListener('click', function () {
            applyTheme(this.getAttribute('data-card-set'), true);
        });
    }

    // 初始化：head 里的内联脚本已经贴过一次属性（避免闪屏），这里补上按钮高亮
    var saved = null;
    try {
        saved = localStorage.getItem(STORAGE_KEY);
    } catch (e) {
        saved = null;
    }

    // 也支持用链接指定配色，比如 /?card=sand —— 方便分享"我这套配色"或演示截图
    var fromUrl = null;
    try {
        fromUrl = new URLSearchParams(window.location.search).get('card');
    } catch (e) {
        fromUrl = null;
    }
    if (fromUrl && THEMES.indexOf(fromUrl) !== -1) {
        applyTheme(fromUrl, true);
    } else {
        applyTheme(saved || root.getAttribute('data-card') || 'snow', false);
    }

    // ---------- ② 下滑淡入 ----------
    var revealItems = document.querySelectorAll('.reveal');
    if (revealItems.length === 0) {
        return;
    }

    // 老浏览器没有 IntersectionObserver：直接全部显示，不能因为新特性让内容看不到
    if (!('IntersectionObserver' in window)) {
        for (var k = 0; k < revealItems.length; k++) {
            revealItems[k].classList.add('is-visible');
        }
        return;
    }

    var observer = new IntersectionObserver(function (entries) {
        for (var j = 0; j < entries.length; j++) {
            if (entries[j].isIntersecting) {
                entries[j].target.classList.add('is-visible');
                observer.unobserve(entries[j].target);   // 只播一次，滚回去不再重播
            }
        }
    }, {
        rootMargin: '0px 0px -12% 0px',   // 元素露出八成以上再触发，别太早
        threshold: 0.08
    });

    for (var m = 0; m < revealItems.length; m++) {
        observer.observe(revealItems[m]);
    }

    /**
     * 兜底：万一观察器没触发（老浏览器、无头截图、某些内嵌浏览器），
     * 就按"元素是否在视口里"手动补一次。
     * 宁可少一个淡入动画，也不能让文章一直看不见。
     */
    function revealInViewport() {
        for (var q = 0; q < revealItems.length; q++) {
            var el = revealItems[q];
            if (el.classList.contains('is-visible')) {
                continue;
            }
            var rect = el.getBoundingClientRect();
            if (rect.top < window.innerHeight && rect.bottom > 0) {
                el.classList.add('is-visible');
            }
        }
    }

    window.addEventListener('load', revealInViewport);
    window.addEventListener('scroll', revealInViewport, { passive: true });
    window.setTimeout(revealInViewport, 1200);
})();
