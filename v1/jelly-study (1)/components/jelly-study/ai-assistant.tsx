"use client"

import { useState, useRef, useEffect } from "react"
import { X, Send, Sparkles, MessageSquare, Network, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

interface Message {
  id: string
  role: "user" | "assistant"
  content: string
}

const initialMessages: Message[] = [
  {
    id: "1",
    role: "assistant",
    content: "你好！我是 JellyStudy 的 AI 学习助手 🎓\n\n我可以帮你解答学习问题、解释复杂概念、提供学习建议。有什么我可以帮助你的吗？",
  },
]

export default function AIAssistant() {
  const [isOpen, setIsOpen] = useState(false)
  const [activeTab, setActiveTab] = useState<"chat" | "knowledge">("chat")
  const [messages, setMessages] = useState<Message[]>(initialMessages)
  const [input, setInput] = useState("")
  const [isTyping, setIsTyping] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const handleSend = async () => {
    if (!input.trim()) return

    const userMessage: Message = {
      id: Date.now().toString(),
      role: "user",
      content: input,
    }

    setMessages((prev) => [...prev, userMessage])
    setInput("")
    setIsTyping(true)

    // Simulate AI response
    setTimeout(() => {
      const aiResponse: Message = {
        id: (Date.now() + 1).toString(),
        role: "assistant",
        content: "这是一个很好的问题！让我来帮你分析一下...\n\n基于你的问题，我建议你可以从以下几个方面入手：\n\n1. 首先理解基本概念\n2. 通过实践加深理解\n3. 尝试解决相关问题\n\n需要我进一步解释某个部分吗？",
      }
      setMessages((prev) => [...prev, aiResponse])
      setIsTyping(false)
    }, 1500)
  }

  return (
    <>
      {/* Floating Button */}
      <button
        onClick={() => setIsOpen(true)}
        className={cn(
          "fixed bottom-6 right-6 z-50 flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-[#667eea] to-[#764ba2] text-white shadow-lg transition-all duration-300",
          "hover:scale-110 hover:shadow-xl hover:shadow-[#667eea]/30",
          "animate-pulse-glow",
          isOpen && "scale-0 opacity-0"
        )}
      >
        <div className="relative">
          <Sparkles className="h-7 w-7 animate-bounce-subtle" />
          {/* Notification Badge */}
          <span className="absolute -right-1 -top-1 flex h-3 w-3">
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-red-400 opacity-75" />
            <span className="relative inline-flex h-3 w-3 rounded-full bg-red-500" />
          </span>
        </div>
      </button>

      {/* Chat Panel */}
      <div
        className={cn(
          "fixed bottom-6 right-6 z-50 flex h-[600px] w-[400px] max-w-[calc(100vw-48px)] flex-col overflow-hidden rounded-2xl border border-border/50 bg-background/95 shadow-2xl shadow-[#667eea]/20 backdrop-blur-xl transition-all duration-500",
          isOpen ? "scale-100 opacity-100" : "pointer-events-none scale-95 opacity-0"
        )}
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-border/50 bg-gradient-to-r from-[#667eea]/10 to-[#764ba2]/10 p-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-[#667eea] to-[#764ba2]">
              <Sparkles className="h-5 w-5 text-white" />
            </div>
            <div>
              <h3 className="font-semibold text-foreground">AI 学习助手</h3>
              <p className="text-xs text-muted-foreground">随时为你解答</p>
            </div>
          </div>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => setIsOpen(false)}
            className="h-8 w-8 rounded-lg hover:bg-destructive/10 hover:text-destructive"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-border/50">
          <button
            onClick={() => setActiveTab("chat")}
            className={cn(
              "flex flex-1 items-center justify-center gap-2 py-3 text-sm font-medium transition-all duration-300",
              activeTab === "chat"
                ? "border-b-2 border-[#667eea] text-[#667eea]"
                : "text-muted-foreground hover:text-foreground"
            )}
          >
            <MessageSquare className="h-4 w-4" />
            对话
          </button>
          <button
            onClick={() => setActiveTab("knowledge")}
            className={cn(
              "flex flex-1 items-center justify-center gap-2 py-3 text-sm font-medium transition-all duration-300",
              activeTab === "knowledge"
                ? "border-b-2 border-[#667eea] text-[#667eea]"
                : "text-muted-foreground hover:text-foreground"
            )}
          >
            <Network className="h-4 w-4" />
            知识图谱
          </button>
        </div>

        {/* Content */}
        {activeTab === "chat" ? (
          <>
            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={cn(
                    "flex",
                    message.role === "user" ? "justify-end" : "justify-start"
                  )}
                >
                  <div
                    className={cn(
                      "max-w-[85%] rounded-2xl px-4 py-3 text-sm leading-relaxed",
                      message.role === "user"
                        ? "bg-gradient-to-r from-[#667eea] to-[#764ba2] text-white"
                        : "bg-muted text-foreground"
                    )}
                  >
                    <p className="whitespace-pre-wrap">{message.content}</p>
                  </div>
                </div>
              ))}
              {isTyping && (
                <div className="flex justify-start">
                  <div className="flex items-center gap-2 rounded-2xl bg-muted px-4 py-3">
                    <Loader2 className="h-4 w-4 animate-spin text-[#667eea]" />
                    <span className="text-sm text-muted-foreground">正在思考...</span>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Input */}
            <div className="border-t border-border/50 p-4">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && handleSend()}
                  placeholder="输入你的问题..."
                  className="flex-1 rounded-xl border border-border/50 bg-muted/50 px-4 py-3 text-sm text-foreground placeholder:text-muted-foreground focus:border-[#667eea]/50 focus:outline-none focus:ring-2 focus:ring-[#667eea]/20"
                />
                <Button
                  onClick={handleSend}
                  disabled={!input.trim() || isTyping}
                  className="rounded-xl bg-gradient-to-r from-[#667eea] to-[#764ba2] px-4 hover:opacity-90"
                >
                  <Send className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </>
        ) : (
          /* Knowledge Graph Tab */
          <div className="flex flex-1 flex-col items-center justify-center p-8 text-center">
            <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-[#667eea]/20 to-[#764ba2]/20">
              <Network className="h-8 w-8 text-[#667eea]" />
            </div>
            <h4 className="mb-2 font-semibold text-foreground">知识图谱可视化</h4>
            <p className="text-sm text-muted-foreground">
              即将上线！将零散知识点串联成网络，让学习更有条理。
            </p>
          </div>
        )}
      </div>
    </>
  )
}
