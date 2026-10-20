/**
 * 阅读体验前端脚本（原生 JS，没有用任何框架）。
 *
 * 只做三类事：
 *   ① 侧边栏「外观」：切换卡片配色（4 套）/ 正文字号 / 行距（3 组设置），
 *      做法都是"往 <html> 上贴一个 data-* 属性"，CSS 里用变量接住；
 *   ② 沉浸模式：给 <html> 贴 data-immersive="on"，CSS 负责收起侧边栏；
 *   ③ 首页下滑时文章列表淡入（IntersectionObserver）。
 *
 * 为什么这些偏好存在浏览器（localStorage）而不是数据库？
 * 因为它们是"这台设备上这个读者的偏好"，管理员不该替访客决定；
 * 存本地不用加接口、不用改表，纯前端就能记住。
 */
(function () {
    'use strict';

    /* ---------- 三类设置的元信息：键名、可选值、默认值、属性名 ---------- */
    var SETTINGS = [
        {
            kind: 'card',
            storageKey: 'blog.cardTheme',
            values: ['snow', 'sand', 'pine', 'ink'],
            fallback: 'snow',
            attr: 'data-card',
            buttonAttr: 'data-card-set'
        },
        {
            kind: 'font',
            storageKey: 'blog.fontSize',
            values: ['sm', 'md', 'lg'],
            fallback: 'md',
            attr: 'data-font',
            buttonAttr: 'data-font-set'
        },
        {
            kind: 'leading',
            storageKey: 'blog.lineHeight',
            values: ['normal', 'loose'],
            fallback: 'normal',
            attr: 'data-leading',
            buttonAttr: 'data-leading-set'
        }
    ];

    var IMMERSIVE = {
        kind: 'immersive',
        storageKey: 'blog.immersive',
        values: ['on', 'off'],
        fallback: 'off',
        attr: 'data-immersive'
    };

    var root = document.documentElement;

    /** 读 URL 上的参数，例如 /?card=ink&font=lg&leading=loose&immersive=on */
    function readFromUrl(name) {
        try {
            return new URLSearchParams(window.location.search).get(name);
        } catch (e) {
            return null;   // 老浏览器没有 URLSearchParams
        }
    }

    /** 读 localStorage，值不合法就当作没设置 */
    function readStored(setting) {
        try {
            var value = localStorage.getItem(setting.storageKey);
            return (value && setting.values.indexOf(value) !== -1) ? value : null;
        } catch (e) {
            return null;   // 隐私模式 / 禁用存储
        }
    }

    /** 写 localStorage（失败也不影响本次生效） */
    function store(setting, value) {
        try {
            localStorage.setItem(setting.storageKey, value);
        } catch (e) {
            /* 忽略 */
        }
    }

    /** 高亮对应的按钮（aria-pressed 既管样式又管无障碍） */
    function syncButtons(setting, value) {
        if (!setting.buttonAttr) {
            return;
        }
        var buttons = document.querySelectorAll('[' + setting.buttonAttr + ']');
        for (var i = 0; i < buttons.length; i++) {
            var isCurrent = buttons[i].getAttribute(setting.buttonAttr) === value;
            buttons[i].setAttribute('aria-pressed', isCurrent ? 'true' : 'false');
        }
    }

    /** 应用设置：贴属性 + 同步按钮 +（可选）存起来 */
    function apply(setting, value, persist) {
        if (setting.values.indexOf(value) === -1) {
            value = setting.fallback;
        }
        if (value === setting.fallback) {
            root.removeAttribute(setting.attr);      // 默认值不写属性，CSS 自然用 :root 的值
        } else {
            root.setAttribute(setting.attr, value);
        }
        syncButtons(setting, value);
        if (persist) {
            store(setting, value);
        }
        return value;
    }

    function currentValue(setting) {
        return root.getAttribute(setting.attr) || setting.fallback;
    }

    /* ---------- ① 三类设置的按钮绑定 ---------- */
    for (var s = 0; s < SETTINGS.length; s++) {
        (function (setting) {
            var buttons = document.querySelectorAll('[' + setting.buttonAttr + ']');
            for (var i = 0; i < buttons.length; i++) {
                buttons[i].addEventListener('click', function () {
                    apply(setting, this.getAttribute(setting.buttonAttr), true);
                });
            }
            // 初始化：优先用 URL 参数（方便分享/演示截图），其次用存储值，
            // 最后用 head 里内联脚本已经贴上的值
            var fromUrl = readFromUrl(setting.kind);
            if (fromUrl && setting.values.indexOf(fromUrl) !== -1) {
                apply(setting, fromUrl, true);
                return;
            }
            apply(setting, readStored(setting) || currentValue(setting), false);
        })(SETTINGS[s]);
    }

    /* ---------- ② 沉浸模式 ---------- */
    function setImmersive(value, persist) {
        var effective = apply(IMMERSIVE, value, persist);
        var toggle = document.querySelector('[data-immersive-toggle]');
        if (toggle) {
            toggle.setAttribute('aria-pressed', effective === 'on' ? 'true' : 'false');
        }
    }

    var immersiveFromUrl = readFromUrl(IMMERSIVE.kind);
    if (immersiveFromUrl && IMMERSIVE.values.indexOf(immersiveFromUrl) !== -1) {
        setImmersive(immersiveFromUrl, true);
    } else {
        setImmersive(readStored(IMMERSIVE) || currentValue(IMMERSIVE), false);
    }

    var immersiveToggle = document.querySelector('[data-immersive-toggle]');
    if (immersiveToggle) {
        immersiveToggle.addEventListener('click', function () {
            var next = currentValue(IMMERSIVE) === 'on' ? 'off' : 'on';
            setImmersive(next, true);
        });
    }

    // 侧边栏收起后唯一的出口
    var immersiveExit = document.getElementById('immersiveExit');
    if (immersiveExit) {
        immersiveExit.addEventListener('click', function () {
            setImmersive('off', true);
        });
    }

    /* ---------- ③ 下滑淡入 ---------- */
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
