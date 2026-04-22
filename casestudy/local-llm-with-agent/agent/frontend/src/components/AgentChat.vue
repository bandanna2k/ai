<template>
  <div id="app">
    <div class="container">
      <h1>🤖 ollama4j Agent</h1>
      <p class="subtitle">Tools: <span class="tool-badge">get_time</span> <span class="tool-badge">calculate</span></p>

      <div class="chat-window" ref="chatWindow">
        <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role]">

          <div v-if="msg.role === 'user'" class="bubble user-bubble">
            {{ msg.text }}
          </div>

          <div v-else class="bubble agent-bubble">
            <div class="answer">{{ msg.text }}</div>
            <div v-if="msg.steps && msg.steps.length" class="steps-toggle">
              <button class="toggle-btn" @click="msg.showSteps = !msg.showSteps">
                {{ msg.showSteps ? '▲ Hide' : '▼ Show' }} reasoning ({{ msg.steps.length }} step{{ msg.steps.length !== 1 ? 's' : '' }})
              </button>
              <div v-if="msg.showSteps" class="steps">
                <div v-for="(step, j) in msg.steps" :key="j" :class="['step', 'step-' + step.type.toLowerCase()]">
                  <span class="step-label">{{ step.type }}</span>
                  <span class="step-content">{{ step.content }}</span>
                </div>
              </div>
            </div>
          </div>

        </div>

        <div v-if="loading" class="message agent">
          <div class="bubble agent-bubble thinking">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>
      </div>

      <form class="input-row" @submit.prevent="sendQuestion">
        <input
          v-model="question"
          class="question-input"
          type="text"
          placeholder="Ask something… e.g. What time is it? or What is 42 * 17?"
          :disabled="loading"
          autofocus
        />
        <button class="send-btn" type="submit" :disabled="loading || !question.trim()">
          Send
        </button>
      </form>

      <p v-if="error" class="error-msg">{{ error }}</p>
    </div>
  </div>
</template>

<script>
import { ref, nextTick } from 'vue'

export default {
  name: 'AgentChat',
  setup() {
    const question = ref('')
    const messages = ref([])
    const loading = ref(false)
    const error = ref('')
    const chatWindow = ref(null)

    async function sendQuestion() {
      const q = question.value.trim()
      if (!q) return

      error.value = ''
      question.value = ''
      messages.value.push({ role: 'user', text: q })
      loading.value = true
      await scrollToBottom()

      try {
        const resp = await fetch('/v1/chat', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ question: q })
        })

        if (!resp.ok) {
          const body = await resp.json().catch(() => ({}))
          throw new Error(body.error || `HTTP ${resp.status}`)
        }

        const data = await resp.json()
        // Filter out the FINAL step since it duplicates the answer
        const displaySteps = (data.steps || []).filter(s => s.type !== 'FINAL')
        messages.value.push({
          role: 'agent',
          text: data.answer,
          steps: displaySteps,
          showSteps: false
        })
      } catch (e) {
        error.value = 'Error: ' + e.message
      } finally {
        loading.value = false
        await scrollToBottom()
      }
    }

    async function scrollToBottom() {
      await nextTick()
      if (chatWindow.value) {
        chatWindow.value.scrollTop = chatWindow.value.scrollHeight
      }
    }

    return { question, messages, loading, error, chatWindow, sendQuestion }
  }
}
</script>

<style scoped>
.container {
  max-width: 700px;
  margin: 0 auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  background: #fff;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 40px);
}

h1 { margin: 0 0 4px; font-size: 1.4rem; }

.subtitle { margin: 0 0 16px; color: #555; font-size: 0.9rem; }

.tool-badge {
  background: #007bff;
  color: #fff;
  border-radius: 4px;
  padding: 2px 7px;
  font-size: 0.8rem;
  margin-right: 4px;
}

.chat-window {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message { display: flex; }
.message.user  { justify-content: flex-end; }
.message.agent { justify-content: flex-start; }

.bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.45;
  font-size: 0.95rem;
}

.user-bubble {
  background: #007bff;
  color: #fff;
  border-bottom-right-radius: 3px;
}

.agent-bubble {
  background: #e9ecef;
  color: #212529;
  border-bottom-left-radius: 3px;
}

.answer { white-space: pre-wrap; }

.steps-toggle { margin-top: 8px; }

.toggle-btn {
  background: none;
  border: 1px solid #adb5bd;
  border-radius: 4px;
  padding: 3px 8px;
  font-size: 0.8rem;
  cursor: pointer;
  color: #495057;
}
.toggle-btn:hover { background: #dee2e6; }

.steps {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.step {
  display: flex;
  gap: 8px;
  font-size: 0.82rem;
  padding: 4px 8px;
  border-radius: 4px;
  background: #fff;
  border-left: 3px solid #adb5bd;
}

.step-thought  { border-left-color: #6f42c1; }
.step-action   { border-left-color: #007bff; }
.step-observation { border-left-color: #28a745; }

.step-label {
  font-weight: bold;
  min-width: 90px;
  color: #495057;
  text-transform: uppercase;
  font-size: 0.75rem;
  padding-top: 1px;
}

.step-content { white-space: pre-wrap; color: #343a40; }

/* Thinking dots */
.thinking { display: flex; align-items: center; gap: 4px; padding: 12px 16px; }
.dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #adb5bd;
  animation: bounce 1.2s infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40%           { transform: translateY(-6px); }
}

.input-row {
  display: flex;
  gap: 8px;
}

.question-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 0.95rem;
}
.question-input:focus { outline: none; border-color: #007bff; }

.send-btn {
  background: #007bff;
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.95rem;
}
.send-btn:hover:not(:disabled) { background: #0056b3; }
.send-btn:disabled { background: #adb5bd; cursor: not-allowed; }

.error-msg {
  margin-top: 8px;
  color: #dc3545;
  font-size: 0.9rem;
}
</style>

