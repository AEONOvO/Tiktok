const input = document.getElementById("input");
const messagesEl = document.getElementById("messages");
const statusEl = document.getElementById("status");
const baseUrlEl = document.getElementById("base-url");
const apiKeyEl = document.getElementById("api-key");
const modelEl = document.getElementById("model");
const systemEl = document.getElementById("system");
const sendBtn = document.getElementById("btn-send");
const clearBtn = document.getElementById("btn-clear");
const rememberEl = document.getElementById("remember");
const toggleKeyBtn = document.getElementById("btn-toggle-key");

const storageKey = "ai_chat_config";
const conversation = [];

function withBridge(fn) {
    if (window.AndroidBridge && typeof fn === "function") {
        fn(window.AndroidBridge);
        return true;
    }
    return false;
}

function setStatus(text, tone) {
    statusEl.textContent = text;
    statusEl.dataset.tone = tone || "";
}

function appendMessage(role, text) {
    const item = document.createElement("div");
    item.className = `message ${role}`;
    const label = document.createElement("div");
    label.className = "label";
    label.textContent = role === "user" ? "你" : "AI";
    const body = document.createElement("div");
    body.className = "body";
    body.textContent = text;
    item.appendChild(label);
    item.appendChild(body);
    messagesEl.appendChild(item);
    messagesEl.scrollTop = messagesEl.scrollHeight;
}

function loadConfig() {
    const raw = localStorage.getItem(storageKey);
    if (!raw) {
        return;
    }
    try {
        const data = JSON.parse(raw);
        baseUrlEl.value = data.baseUrl || "";
        apiKeyEl.value = data.apiKey || "";
        modelEl.value = data.model || "";
        systemEl.value = data.system || "";
        rememberEl.checked = Boolean(data.remember);
    } catch (error) {
        console.warn("Config parse error", error);
    }
}

function saveConfig() {
    if (!rememberEl.checked) {
        localStorage.removeItem(storageKey);
        return;
    }
    const payload = {
        baseUrl: baseUrlEl.value.trim(),
        apiKey: apiKeyEl.value.trim(),
        model: modelEl.value.trim(),
        system: systemEl.value.trim(),
        remember: true,
    };
    localStorage.setItem(storageKey, JSON.stringify(payload));
}

function getConfig() {
    const baseUrl = baseUrlEl.value.trim();
    const apiKey = apiKeyEl.value.trim();
    const model = modelEl.value.trim();
    const system = systemEl.value.trim();
    return { baseUrl, apiKey, model, system };
}

async function sendMessage() {
    const text = input.value.trim();
    if (!text) {
        setStatus("请输入内容", "warn");
        return;
    }

    const { baseUrl, apiKey, model, system } = getConfig();
    if (!baseUrl || !apiKey || !model) {
        setStatus("请先补全 Base URL / API Key / Model", "warn");
        return;
    }

    saveConfig();
    appendMessage("user", text);
    conversation.push({ role: "user", content: text });
    input.value = "";

    sendBtn.disabled = true;
    setStatus("请求中...", "pending");

    const endpoint = baseUrl.replace(/\/$/, "") + "/v1/chat/completions";
    const messages = system
        ? [{ role: "system", content: system }, ...conversation]
        : [...conversation];

    try {
        const response = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${apiKey}`,
            },
            body: JSON.stringify({
                model,
                messages,
                temperature: 0.7,
            }),
        });

        if (!response.ok) {
            const detail = await response.text();
            throw new Error(detail || `HTTP ${response.status}`);
        }

        const data = await response.json();
        const reply =
            data && data.choices && data.choices[0] && data.choices[0].message
                ? data.choices[0].message.content
                : "未获取到回复";

        appendMessage("assistant", reply.trim());
        conversation.push({ role: "assistant", content: reply });
        setStatus("已完成", "ok");
    } catch (error) {
        appendMessage("assistant", "请求失败，请检查配置或网络。\n" + error.message);
        setStatus("请求失败", "error");
    } finally {
        sendBtn.disabled = false;
    }
}

document.getElementById("btn-native").addEventListener("click", () => {
    withBridge((bridge) => bridge.openNativeDialog("来自 H5 的问候"));
});

sendBtn.addEventListener("click", sendMessage);
input.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
});

clearBtn.addEventListener("click", () => {
    conversation.length = 0;
    messagesEl.innerHTML = "";
    setStatus("已清空", "ok");
});

baseUrlEl.addEventListener("change", saveConfig);
apiKeyEl.addEventListener("change", saveConfig);
modelEl.addEventListener("change", saveConfig);
systemEl.addEventListener("change", saveConfig);
rememberEl.addEventListener("change", saveConfig);
toggleKeyBtn.addEventListener("click", () => {
    const isHidden = apiKeyEl.type === "password";
    apiKeyEl.type = isHidden ? "text" : "password";
    toggleKeyBtn.textContent = isHidden ? "隐藏 Key" : "显示 Key";
});

loadConfig();
setStatus("就绪", "ok");
