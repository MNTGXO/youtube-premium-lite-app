package com.example.youtubepremiumlite;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // The full userscript (copy‐pasted from the provided script)
    private static final String USERSCRIPT =
            "// ==UserScript==\n" +
            "// @name YouTube Premium Lite² [Beta]\n" +
            "// @name:zh-TW YouTube Premium Lite² [Beta 測試版]\n" +
            "// @name:zh-CN YouTube Premium Lite² [Beta 测试版]\n" +
            "// @name:ja YouTube Premium Lite² [ベータ版]\n" +
            "// @icon https://www.google.com/s2/favicons?sz=64&domain=youtube.com\n" +
            "// @author ElectroKnight22\n" +
            "// @namespace electroknight22_youtube_premium_lite_squared_namespace\n" +
            "// @version 0.6\n" +
            "// @match *://m.youtube.com/*\n" +
            "// @match *://www.youtube.com/*\n" +
            "// @exclude *://www.youtube.com/live_chat*\n" +
            "// @grant none\n" +
            "// @run-at document-start\n" +
            "// @inject-into page\n" +
            "// @license MIT\n" +
            "// @description Have you ever wanted Premium but think that the real thing is too good? Worry not, this script replaces the loser normal logo with the premium one, tricking you and your friends into thinking you are richer than you really are. Currently still testing so functionality may be rough or incomplete in some places. Please be understanding.\n" +
            "// @description:zh-TW 是否曾渴望擁有 Premium，卻又覺得正版太過美好？別擔心，這個腳本會將那魯蛇般的普通標誌替換為 Premium 圖示，讓你和你的朋友們誤以為你比實際上更有錢。目前仍在測試階段，因此部分功能可能會不穩定或不完整，請多見諒。\n" +
            "// @description:zh-CN 是否曾渴望拥有 Premium，却又觉得正版太过美好？别担心，这个脚本会将那有些寒酸的普通标志替换为 Premium 图标，让你和你的朋友们误以为你比实际上更有钱。目前仍在测试阶段，因此部分功能可能会不稳定或不完整，请多见谅。\n" +
            "// @description:ja Premium が欲しいけど、本物は立派すぎて手が出せないと思ったことはありませんか？ご心配なく。このスクリプトは、あの負け犬っぽい普通のロゴを Premium のロゴに置き換え、あなたや友達に「実は金持ちなんじゃないか」と勘違いさせることができます。現在まだテスト中のため、一部の機能が不安定であったり、不完全な場合があります。ご了承ください。\n" +
            "// ==/UserScript==\n" +
            "\n" +
            "/*jshint esversion: 11 */\n" +
            "/* global youtubeHelperApi */\n" +
            "\n" +
            "(function () {\n" +
            "'use strict';\n" +
            "\n" +
            "const HELPERS = {\n" +
            "debounce(func, delay) {\n" +
            "let timer = null;\n" +
            "return function (...args) {\n" +
            "if (timer) clearTimeout(timer);\n" +
            "timer = setTimeout(() => func.apply(this, args), delay);\n" +
            "};\n" +
            "},\n" +
            "};\n" +
            "\n" +
            "// == code attribution ==\n" +
            "// RESPONSE_INTERCEPTOR adapted from the 'YouTube Ad Bypass' script by 'nec.d'.\n" +
            "// Author GreasyFork page link: https://greasyfork.org/en/users/929955-nec-d\n" +
            "// v1.8 script code link: https://greasyfork.org/en/scripts/566949-youtube-ad-bypass?version=1819714\n" +
            "// ==\n" +
            "const RESPONSE_INTERCEPTOR = (function () {\n" +
            "const AD_FIELDS = ['adPlacements', 'playerAds', 'adSlots', 'adBreakHeartbeatParams', 'adBreakParams'];\n" +
            "const ENFORCEMENT_FIELDS = [\n" +
            "'enforcementMessageViewModel',\n" +
            "'enforcementMessage',\n" +
            "'adBlockerOverlay',\n" +
            "'adBlockDetected',\n" +
            "];\n" +
            "const NATIVE_METHODS = {\n" +
            "JSON_parse: window.JSON.parse,\n" +
            "Response_json: window.Response.prototype.json,\n" +
            "window_fetch: window.fetch,\n" +
            "XHR_open: XMLHttpRequest.prototype.open,\n" +
            "XHR_send: XMLHttpRequest.prototype.send,\n" +
            "classAdd: DOMTokenList.prototype.add,\n" +
            "};\n" +
            "\n" +
            "function isAdVideoUrl(url) {\n" +
            "return typeof url === 'string' && url.includes('googlevideo.com/videoplayback') && /[?&]ctier=/.test(url);\n" +
            "}\n" +
            "\n" +
            "function stripAds(obj) {\n" +
            "if (!obj || typeof obj !== 'object') return obj;\n" +
            "\n" +
            "let found = false;\n" +
            "for (const field of AD_FIELDS) {\n" +
            "if (field in obj) {\n" +
            "const val = obj[field];\n" +
            "if (Array.isArray(val)) val.length = 0;\n" +
            "delete obj[field];\n" +
            "found = true;\n" +
            "}\n" +
            "}\n" +
            "\n" +
            "if (found) obj.adThrottled = true;\n" +
            "\n" +
            "if (obj.playerResponse && typeof obj.playerResponse === 'object') {\n" +
            "stripAds(obj.playerResponse);\n" +
            "}\n" +
            "\n" +
            "if (Array.isArray(obj)) {\n" +
            "for (const item of obj) {\n" +
            "if (item && typeof item === 'object') stripAds(item);\n" +
            "}\n" +
            "}\n" +
            "return obj;\n" +
            "}\n" +
            "\n" +
            "function hasAdData(obj) {\n" +
            "if (!obj || typeof obj !== 'object') return false;\n" +
            "if (Array.isArray(obj)) {\n" +
            "return obj.some(\n" +
            "(item) =>\n" +
            "item?.adPlacements || item?.playerAds || item?.adSlots || item?.playerResponse?.adPlacements,\n" +
            ");\n" +
            "}\n" +
            "return !!(\n" +
            "obj.adPlacements\n" +
            "|| obj.playerAds\n" +
            "|| obj.adSlots\n" +
            "|| obj.playerResponse?.adPlacements\n" +
            "|| obj.playerResponse?.playerAds\n" +
            "|| obj.playerResponse?.adSlots\n" +
            ");\n" +
            "}\n" +
            "\n" +
            "function stripEnforcement(obj, depth) {\n" +
            "if (!obj || typeof obj !== 'object' || depth > 12) return false;\n" +
            "let stripped = false;\n" +
            "\n" +
            "if (Array.isArray(obj)) {\n" +
            "for (let i = obj.length - 1; i >= 0; i--) {\n" +
            "const item = obj[i];\n" +
            "if (item && typeof item === 'object') {\n" +
            "if (\n" +
            "item.openPopupAction?.popup?.enforcementMessageViewModel\n" +
            "|| item.openPopupAction?.popup?.confirmDialogRenderer\n" +
            ") {\n" +
            "const s = JSON.stringify(item);\n" +
            "if (s.includes('nforcement') || s.includes('dBlocker') || s.includes('nterrupt')) {\n" +
            "obj.splice(i, 1);\n" +
            "stripped = true;\n" +
            "continue;\n" +
            "}\n" +
            "}\n" +
            "if (stripEnforcement(item, depth + 1)) stripped = true;\n" +
            "}\n" +
            "}\n" +
            "return stripped;\n" +
            "}\n" +
            "\n" +
            "for (const field of ENFORCEMENT_FIELDS) {\n" +
            "if (field in obj) {\n" +
            "delete obj[field];\n" +
            "stripped = true;\n" +
            "}\n" +
            "}\n" +
            "\n" +
            "if (Array.isArray(obj.actions)) {\n" +
            "const before = obj.actions.length;\n" +
            "obj.actions = obj.actions.filter((action) => {\n" +
            "if (!action || typeof action !== 'object') return true;\n" +
            "const popup = action.openPopupAction?.popup || action.showDialogCommand?.dialog;\n" +
            "if (!popup) return true;\n" +
            "if (popup.enforcementMessageViewModel || popup.confirmDialogRenderer) {\n" +
            "const s = JSON.stringify(popup).substring(0, 2000);\n" +
            "if (s.includes('nforcement') || s.includes('dBlocker') || s.includes('nterrupt')) return false;\n" +
            "}\n" +
            "return true;\n" +
            "});\n" +
            "if (obj.actions.length < before) stripped = true;\n" +
            "}\n" +
            "\n" +
            "for (const key of Object.keys(obj)) {\n" +
            "const val = obj[key];\n" +
            "if (val && typeof val === 'object') {\n" +
            "if (stripEnforcement(val, depth + 1)) stripped = true;\n" +
            "}\n" +
            "}\n" +
            "return stripped;\n" +
            "}\n" +
            "\n" +
            "function setObjectTraps(enabled) {\n" +
            "const BLOCKED_CLASSES = new Set(['ad-showing', 'ad-interrupting']);\n" +
            "\n" +
            "if (enabled) {\n" +
            "const trapProperty = (name) => {\n" +
            "let value;\n" +
            "try {\n" +
            "value = window[name];\n" +
            "Object.defineProperty(window, name, {\n" +
            "get: () => value,\n" +
            "set: (v) => {\n" +
            "if (v && typeof v === 'object') {\n" +
            "if (hasAdData(v)) stripAds(v);\n" +
            "try {\n" +
            "stripEnforcement(v, 0);\n" +
            "} catch (_) {}\n" +
            "}\n" +
            "value = v;\n" +
            "},\n" +
            "configurable: true,\n" +
            "enumerable: true,\n" +
            "});\n" +
            "} catch (e) {\n" +
            "console.warn('Failed to trap ' + name, e);\n" +
            "}\n" +
            "};\n" +
            "\n" +
            "trapProperty('ytInitialPlayerResponse');\n" +
            "trapProperty('ytInitialData');\n" +
            "\n" +
            "DOMTokenList.prototype.add = function (...tokens) {\n" +
            "const filtered = tokens.filter((t) => !BLOCKED_CLASSES.has(t));\n" +
            "// FIXED: Using NATIVE_METHODS.classAdd instead of nativeClassAdd\n" +
            "return filtered.length ? NATIVE_METHODS.classAdd.apply(this, filtered) : undefined;\n" +
            "};\n" +
            "} else {\n" +
            "const restoreProperty = (name) => {\n" +
            "try {\n" +
            "const currentValue = window[name];\n" +
            "delete window[name];\n" +
            "window[name] = currentValue;\n" +
            "} catch (e) {\n" +
            "console.warn('Failed to restore ' + name, e);\n" +
            "}\n" +
            "};\n" +
            "\n" +
            "restoreProperty('ytInitialPlayerResponse');\n" +
            "restoreProperty('ytInitialData');\n" +
            "\n" +
            "// FIXED: Using NATIVE_METHODS.classAdd\n" +
            "DOMTokenList.prototype.add = NATIVE_METHODS.classAdd;\n" +
            "}\n" +
            "}\n" +
            "\n" +
            "const NetworkTraps = {\n" +
            "setJsonParseTrap(enabled) {\n" +
            "if (enabled) {\n" +
            "window.JSON.parse = function (text, reviver) {\n" +
            "const result = NATIVE_METHODS.JSON_parse.call(this, text, reviver);\n" +
            "try {\n" +
            "if (result && typeof result === 'object') {\n" +
            "if (hasAdData(result)) stripAds(result);\n" +
            "if (\n" +
            "typeof text === 'string'\n" +
            "&& text.length > 200\n" +
            "&& (text.includes('nforcement')\n" +
            "|| text.includes('dBlocker')\n" +
            "|| text.includes('nterrupt'))\n" +
            ") {\n" +
            "stripEnforcement(result, 0);\n" +
            "}\n" +
            "}\n" +
            "} catch (e) {}\n" +
            "return result;\n" +
            "};\n" +
            "} else {\n" +
            "window.JSON.parse = NATIVE_METHODS.JSON_parse;\n" +
            "}\n" +
            "},\n" +
            "\n" +
            "setResponseJsonTrap(enabled) {\n" +
            "if (enabled) {\n" +
            "const YT_API_PATHS = ['/youtubei/v1/', '/get_midroll_', '/player?', '/next?'];\n" +
            "window.Response.prototype.json = async function () {\n" +
            "const data = await NATIVE_METHODS.Response_json.call(this);\n" +
            "try {\n" +
            "const url = this.url || '';\n" +
            "if (data && typeof data === 'object' && YT_API_PATHS.some((p) => url.includes(p))) {\n" +
            "if (hasAdData(data)) stripAds(data);\n" +
            "stripEnforcement(data, 0);\n" +
            "}\n" +
            "} catch (e) {}\n" +
            "return data;\n" +
            "};\n" +
            "} else {\n" +
            "window.Response.prototype.json = NATIVE_METHODS.Response_json;\n" +
            "}\n" +
            "},\n" +
            "\n" +
            "setFetchTrap(enabled) {\n" +
            "if (enabled) {\n" +
            "window.fetch = function (input, init) {\n" +
            "const url = typeof input === 'string' ? input : input?.url || '';\n" +
            "if (isAdVideoUrl(url)) return Promise.resolve(new Response('', { status: 204 }));\n" +
            "return NATIVE_METHODS.window_fetch.call(this, input, init);\n" +
            "};\n" +
            "} else {\n" +
            "window.fetch = NATIVE_METHODS.window_fetch;\n" +
            "}\n" +
            "},\n" +
            "\n" +
            "setXhrTrap(enabled) {\n" +
            "if (enabled) {\n" +
            "XMLHttpRequest.prototype.open = function (method, url) {\n" +
            "this._ytAdBlocked = isAdVideoUrl(url);\n" +
            "return NATIVE_METHODS.XHR_open.apply(this, arguments);\n" +
            "};\n" +
            "\n" +
            "XMLHttpRequest.prototype.send = function () {\n" +
            "if (this._ytAdBlocked) {\n" +
            "Object.defineProperty(this, 'readyState', { value: 4 });\n" +
            "Object.defineProperty(this, 'status', { value: 204 });\n" +
            "Object.defineProperty(this, 'responseText', { value: '' });\n" +
            "this.dispatchEvent(new Event('readystatechange'));\n" +
            "this.dispatchEvent(new Event('load'));\n" +
            "this.dispatchEvent(new Event('loadend'));\n" +
            "return;\n" +
            "}\n" +
            "return NATIVE_METHODS.XHR_send.apply(this, arguments);\n" +
            "};\n" +
            "} else {\n" +
            "XMLHttpRequest.prototype.open = NATIVE_METHODS.XHR_open;\n" +
            "XMLHttpRequest.prototype.send = NATIVE_METHODS.XHR_send;\n" +
            "}\n" +
            "},\n" +
            "};\n" +
            "\n" +
            "return {\n" +
            "setAllTraps(enabled) {\n" +
            "setObjectTraps(enabled);\n" +
            "NetworkTraps.setJsonParseTrap(enabled);\n" +
            "NetworkTraps.setResponseJsonTrap(enabled);\n" +
            "NetworkTraps.setFetchTrap(enabled);\n" +
            "NetworkTraps.setXhrTrap(enabled);\n" +
            "},\n" +
            "\n" +
            "delayedInitialize() {\n" +
            "NetworkTraps.setFetchTrap(true);\n" +
            "},\n" +
            "initialize() {\n" +
            "setObjectTraps(true);\n" +
            "NetworkTraps.setJsonParseTrap(true);\n" +
            "NetworkTraps.setResponseJsonTrap(true);\n" +
            "NetworkTraps.setXhrTrap(true);\n" +
            "},\n" +
            "};\n" +
            "})();\n" +
            "\n" +
            "const STYLE_INJECTION_MODULE = {\n" +
            "state: {\n" +
            "isFixing: false,\n" +
            "container: null,\n" +
            "observer: null,\n" +
            "forceFullRecalc: false,\n" +
            "},\n" +
            "visibilityLock: 0,\n" +
            "runLayoutFix: null,\n" +
            "\n" +
            "setAdBlockingStyles() {\n" +
            "const cssRules = `\n" +
            "#ad-created,\n" +
            "#ytd-in-feed-ad-layout-renderer,\n" +
            "ytd-in-feed-ad-layout-renderer,\n" +
            "ytd-banner-promo-renderer,\n" +
            "ytd-ad-slot-renderer,\n" +
            "ytd-rich-item-renderer:has(ytd-ad-slot-renderer),\n" +
            "ytd-rich-item-renderer:has(ytd-in-feed-ad-layout-renderer),\n" +
            ".ytd-ad-slot-renderer,\n" +
            "yt-mealbar-promo-renderer,\n" +
            "ytd-engagement-panel-section-list-renderer[target-id='engagement-panel-ads'],\n" +
            "ytd-player-legacy-desktop-watch-ads-renderer,\n" +
            "ytd-single-option-survey-renderer,\n" +
            "ytd-enforcement-message-view-model,\n" +
            "tp-yt-paper-dialog:has(ytd-enforcement-message-view-model),\n" +
            "yt-notification-action-renderer:has(yt-button-renderer a[href*=\"help.youtube.com/answer/14746816\"]) {\n" +
            "display: none !important;\n" +
            "}\n" +
            "tp-yt-iron-overlay-backdrop.opened {\n" +
            "display: none !important;\n" +
            "}\n" +
            "`;\n" +
            "\n" +
            "const promoRefuserStyleNode = document.createElement('style');\n" +
            "promoRefuserStyleNode.id = 'yt-premium-lite-ads-refuse-css';\n" +
            "promoRefuserStyleNode.textContent = cssRules;\n" +
            "document.documentElement.appendChild(promoRefuserStyleNode);\n" +
            "},\n" +
            "\n" +
            "setCustomGridStyles() {\n" +
            "const gridCss = `\n" +
            "ytd-rich-item-renderer[rendered-from-rich-grid][is-in-first-column] {\n" +
            "margin-left: calc(var(--ytd-rich-grid-item-margin) / 2) !important;\n" +
            "}\n" +
            "ytd-rich-item-renderer[rendered-from-rich-grid][data-is-first-col=\"true\"] {\n" +
            "margin-left: calc(var(--ytd-rich-grid-item-margin) / 2 + var(--ytd-rich-grid-gutter-margin)) !important;\n" +
            "}\n" +
            "ytd-rich-grid-renderer ytd-continuation-item-renderer {\n" +
            "transform: translateY(-1500px);\n" +
            "pointer-events: none;\n" +
            "opacity: 0;\n" +
            "}\n" +
            "`;\n" +
            "const styleNode = document.createElement('style');\n" +
            "styleNode.id = 'yt-premium-lite-grid-css';\n" +
            "styleNode.textContent = gridCss;\n" +
            "document.documentElement.appendChild(styleNode);\n" +
            "},\n" +
            "\n" +
            "invalidateCache() {\n" +
            "if (STYLE_INJECTION_MODULE.state.observer) {\n" +
            "STYLE_INJECTION_MODULE.state.observer.disconnect();\n" +
            "STYLE_INJECTION_MODULE.state.observer = null;\n" +
            "}\n" +
            "STYLE_INJECTION_MODULE.state.container = null;\n" +
            "},\n" +
            "\n" +
            "isNodeVisible(node) {\n" +
            "return node.offsetWidth > 0 && node.offsetHeight > 0 && getComputedStyle(node).display !== 'none';\n" +
            "},\n" +
            "\n" +
            "fixHomepageLayout() {\n" +
            "if (window.location.pathname !== '/') return;\n" +
            "if (STYLE_INJECTION_MODULE.state.isFixing) return;\n" +
            "\n" +
            "try {\n" +
            "STYLE_INJECTION_MODULE.state.isFixing = true;\n" +
            "\n" +
            "if (!STYLE_INJECTION_MODULE.state.container || !STYLE_INJECTION_MODULE.state.container.isConnected) {\n" +
            "const beacon = document.querySelector('ytd-rich-item-renderer');\n" +
            "if (!beacon) return (STYLE_INJECTION_MODULE.state.isFixing = false);\n" +
            "STYLE_INJECTION_MODULE.state.container = beacon.parentElement;\n" +
            "\n" +
            "STYLE_INJECTION_MODULE.state.observer = new MutationObserver(() => {\n" +
            "if (!STYLE_INJECTION_MODULE.state.isFixing) STYLE_INJECTION_MODULE.runLayoutFix();\n" +
            "});\n" +
            "STYLE_INJECTION_MODULE.state.observer.observe(STYLE_INJECTION_MODULE.state.container, {\n" +
            "childList: true,\n" +
            "});\n" +
            "}\n" +
            "\n" +
            "const beacon = STYLE_INJECTION_MODULE.state.container.querySelector('ytd-rich-item-renderer');\n" +
            "if (!beacon) return (STYLE_INJECTION_MODULE.state.isFixing = false);\n" +
            "\n" +
            "const rawItemsPerRow =\n" +
            "beacon.getAttribute('items-per-row')\n" +
            "|| beacon.style.getPropertyValue('--ytd-rich-grid-items-per-row');\n" +
            "const itemsPerRow = parseInt(rawItemsPerRow, 10);\n" +
            "\n" +
            "if (!itemsPerRow || isNaN(itemsPerRow)) return (STYLE_INJECTION_MODULE.state.isFixing = false);\n" +
            "\n" +
            "const shelves = Array.from(\n" +
            "STYLE_INJECTION_MODULE.state.container.querySelectorAll('ytd-rich-section-renderer'),\n" +
            ");\n" +
            "\n" +
            "shelves.forEach((shelf) => {\n" +
            "const rect = shelf.getBoundingClientRect();\n" +
            "\n" +
            "if (!STYLE_INJECTION_MODULE.state.forceFullRecalc && rect.bottom < -500) return;\n" +
            "\n" +
            "let visibleCount = 0;\n" +
            "let sibling = shelf.previousElementSibling;\n" +
            "\n" +
            "while (sibling) {\n" +
            "if (sibling.tagName.toLowerCase() === 'ytd-rich-section-renderer') break;\n" +
            "if (\n" +
            "sibling.tagName.toLowerCase() === 'ytd-rich-item-renderer'\n" +
            "&& STYLE_INJECTION_MODULE.isNodeVisible(sibling)\n" +
            ") {\n" +
            "visibleCount++;\n" +
            "}\n" +
            "sibling = sibling.previousElementSibling;\n" +
            "}\n" +
            "\n" +
            "const remainder = visibleCount % itemsPerRow;\n" +
            "const needed = remainder === 0 ? 0 : itemsPerRow - remainder;\n" +
            "\n" +
            "if (needed > 0) {\n" +
            "const itemsToMove = [];\n" +
            "let candidate = shelf.nextElementSibling;\n" +
            "let guard = 0;\n" +
            "\n" +
            "while (candidate && itemsToMove.length < needed && guard < 50) {\n" +
            "guard++;\n" +
            "const isShelf = candidate.tagName.toLowerCase() === 'ytd-rich-section-renderer';\n" +
            "const isItem = candidate.tagName.toLowerCase() === 'ytd-rich-item-renderer';\n" +
            "\n" +
            "if (isShelf) break;\n" +
            "if (!candidate.nextElementSibling && !isShelf) break;\n" +
            "\n" +
            "if (isItem && STYLE_INJECTION_MODULE.isNodeVisible(candidate)) itemsToMove.push(candidate);\n" +
            "candidate = candidate.nextElementSibling;\n" +
            "}\n" +
            "\n" +
            "if (itemsToMove.length > 0) {\n" +
            "itemsToMove.forEach((item) =>\n" +
            "STYLE_INJECTION_MODULE.state.container.insertBefore(item, shelf),\n" +
            ");\n" +
            "}\n" +
            "}\n" +
            "});\n" +
            "\n" +
            "const children = STYLE_INJECTION_MODULE.state.container.children;\n" +
            "const updates = [];\n" +
            "\n" +
            "let visualColumn = 0;\n" +
            "\n" +
            "for (let i = 0; i < children.length; i++) {\n" +
            "const child = children[i];\n" +
            "const tagName = child.tagName.toLowerCase();\n" +
            "\n" +
            "if (!STYLE_INJECTION_MODULE.isNodeVisible(child)) continue;\n" +
            "\n" +
            "if (tagName === 'ytd-rich-section-renderer') {\n" +
            "if (visualColumn > 0) visualColumn = 0;\n" +
            "continue;\n" +
            "}\n" +
            "\n" +
            "if (tagName === 'ytd-rich-item-renderer') {\n" +
            "const isFirstColumn = visualColumn === 0;\n" +
            "const currentlyFirst = child.hasAttribute('is-in-first-column');\n" +
            "\n" +
            "if (isFirstColumn && !currentlyFirst) {\n" +
            "updates.push({ node: child, val: 'true' });\n" +
            "} else if (!isFirstColumn && currentlyFirst) {\n" +
            "updates.push({ node: child, val: 'false' });\n" +
            "}\n" +
            "\n" +
            "visualColumn++;\n" +
            "if (visualColumn >= itemsPerRow) visualColumn = 0;\n" +
            "}\n" +
            "}\n" +
            "\n" +
            "for (const update of updates) {\n" +
            "if (update.val === 'true') {\n" +
            "update.node.setAttribute('is-in-first-column', '');\n" +
            "} else {\n" +
            "update.node.removeAttribute('is-in-first-column');\n" +
            "}\n" +
            "}\n" +
            "} catch (error) {\n" +
            "console.error('[YouTube Premium Lite²] Layout Fix Error:', error);\n" +
            "} finally {\n" +
            "STYLE_INJECTION_MODULE.state.isFixing = false;\n" +
            "STYLE_INJECTION_MODULE.state.forceFullRecalc = false;\n" +
            "}\n" +
            "},\n" +
            "\n" +
            "handleWindowResize() {\n" +
            "STYLE_INJECTION_MODULE.state.forceFullRecalc = true;\n" +
            "if (STYLE_INJECTION_MODULE.runLayoutFix) STYLE_INJECTION_MODULE.runLayoutFix();\n" +
            "},\n" +
            "\n" +
            "initialize() {\n" +
            "if (window.ytInitialData?.topbar?.desktopTopbarRenderer?.logo?.topbarLogoRenderer?.iconImage) {\n" +
            "window.ytInitialData.topbar.desktopTopbarRenderer.logo.topbarLogoRenderer.iconImage.iconType =\n" +
            "'YOUTUBE_PREMIUM_LOGO';\n" +
            "}\n" +
            "\n" +
            "if (window.yt?.config_?.openPopupConfig?.supportedPopups) {\n" +
            "Object.keys(window.yt.config_.openPopupConfig.supportedPopups).forEach(\n" +
            "(k) =>\n" +
            "k.split(/(?=[A-Z])/).some((w) => /^(ad|survey|upgrade|offers?|feedback)$/i.test(w))\n" +
            "&& (window.yt.config_.openPopupConfig.supportedPopups[k] = false),\n" +
            ");\n" +
            "}\n" +
            "\n" +
            "STYLE_INJECTION_MODULE.runLayoutFix = HELPERS.debounce(STYLE_INJECTION_MODULE.fixHomepageLayout, 50);\n" +
            "\n" +
            "STYLE_INJECTION_MODULE.setAdBlockingStyles();\n" +
            "STYLE_INJECTION_MODULE.setCustomGridStyles();\n" +
            "STYLE_INJECTION_MODULE.runLayoutFix();\n" +
            "\n" +
            "document.addEventListener('yt-navigate-finish', () => {\n" +
            "STYLE_INJECTION_MODULE.invalidateCache();\n" +
            "// Re-assert styles in case YouTube wiped them during routing\n" +
            "if (!document.getElementById('yt-premium-lite-ads-refuse-css'))\n" +
            "STYLE_INJECTION_MODULE.setAdBlockingStyles();\n" +
            "if (!document.getElementById('yt-premium-lite-grid-css')) STYLE_INJECTION_MODULE.setCustomGridStyles();\n" +
            "});\n" +
            "document.addEventListener('yt-text-inline-expander-expanded-changed', () => {\n" +
            "if (STYLE_INJECTION_MODULE.runLayoutFix) STYLE_INJECTION_MODULE.runLayoutFix();\n" +
            "});\n" +
            "document.addEventListener('yt-request-elements-per-row', STYLE_INJECTION_MODULE.handleWindowResize);\n" +
            "window.addEventListener('resize', STYLE_INJECTION_MODULE.handleWindowResize);\n" +
            "},\n" +
            "\n" +
            "// CSS rule to remove video ad elements. Currently problematic and only kept for archival purposes.\n" +
            "videoAdBlockCssRule: `\n" +
            ".player-ads,\n" +
            ".video-ads.ytp-ad-module,\n" +
            ".html5-video-player.ad-showing video,\n" +
            ".html5-video-player.ad-interrupting video,\n" +
            ".ytp-ad-persistent-progress-bar-container,\n" +
            ".ytp-ad-persistent-progress-bar,\n" +
            ".ad-interrupting .ytp-play-progress.ytp-swatch-background-color {\n" +
            "display: none !important;\n" +
            "}\n" +
            "`,\n" +
            "};\n" +
            "\n" +
            "function onDocumentReady() {\n" +
            "if (\n" +
            "window.yt?.config_?.LOGGED_IN\n" +
            "&& window.ytInitialData?.topbar?.desktopTopbarRenderer?.logo?.topbarLogoRenderer?.iconImage?.iconType\n" +
            "=== 'YOUTUBE_PREMIUM_LOGO'\n" +
            ") {\n" +
            "console.log('Already using YouTube Premium. No need to run this script.');\n" +
            "RESPONSE_INTERCEPTOR.setAllTraps(false);\n" +
            "return;\n" +
            "}\n" +
            "RESPONSE_INTERCEPTOR.delayedInitialize();\n" +
            "STYLE_INJECTION_MODULE.initialize();\n" +
            "}\n" +
            "\n" +
            "function initialize() {\n" +
            "RESPONSE_INTERCEPTOR.initialize();\n" +
            "if (document.readyState === 'loading') {\n" +
            "document.addEventListener('DOMContentLoaded', onDocumentReady);\n" +
            "} else {\n" +
            "onDocumentReady();\n" +
            "}\n" +
            "}\n" +
            "\n" +
            "initialize();\n" +
            "\n" +
            "})();";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setUserAgentString(settings.getUserAgentString().replace("; wv", "")); // optional

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                // Inject the script as early as possible (document-start equivalent)
                view.evaluateJavascript(
                        "javascript:(function() { " +
                        "var script = document.createElement('script');" +
                        "script.textContent = `" + USERSCRIPT.replace("`", "\\`").replace("$", "\\$") + "`;" +
                        "document.documentElement.appendChild(script);" +
                        "})();",
                        null
                );
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("https://m.youtube.com");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
