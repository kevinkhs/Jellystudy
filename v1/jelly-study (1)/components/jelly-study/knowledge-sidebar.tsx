"use client"

import { TrendingUp, Hash } from "lucide-react"
import { Badge } from "@/components/ui/badge"

const hotKnowledgePoints = [
  { name: "数据结构", count: 156, trend: "+12%" },
  { name: "算法", count: 142, trend: "+8%" },
  { name: "React", count: 128, trend: "+15%" },
  { name: "Python", count: 115, trend: "+5%" },
  { name: "数据库", count: 98, trend: "+10%" },
  { name: "机器学习", count: 87, trend: "+20%" },
  { name: "分布式系统", count: 76, trend: "+7%" },
  { name: "前端工程化", count: 65, trend: "+18%" },
]

const quickActions = [
  { icon: "📝", label: "提问", desc: "分享你的问题" },
  { icon: "🔍", label: "搜索", desc: "查找知识点" },
  { icon: "🤖", label: "AI 助手", desc: "智能问答" },
  { icon: "📊", label: "统计", desc: "学习数据" },
]

export default function KnowledgeSidebar() {
  return (
    <section className="py-16">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Main Content - Knowledge Points */}
          <div className="lg:col-span-2">
            <div className="rounded-2xl border border-border/50 bg-card/50 p-6 backdrop-blur-sm">
              <h2 className="mb-6 flex items-center gap-3 text-lg font-semibold text-foreground">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-[#667eea] to-[#764ba2]">
                  <Hash className="h-4 w-4 text-white" />
                </div>
                热门知识点
              </h2>

              <div className="grid gap-3 sm:grid-cols-2">
                {hotKnowledgePoints.map((point, index) => (
                  <div
                    key={point.name}
                    className="group flex items-center justify-between rounded-xl border border-transparent bg-muted/30 p-4 transition-all duration-300 hover:border-[#667eea]/30 hover:bg-[#667eea]/5"
                    style={{ animationDelay: `${index * 0.05}s` }}
                  >
                    <div className="flex items-center gap-3">
                      <Badge
                        variant="outline"
                        className="border-[#667eea]/30 bg-[#667eea]/10 text-[#667eea]"
                      >
                        #{index + 1}
                      </Badge>
                      <span className="font-medium text-foreground transition-colors group-hover:text-[#667eea]">
                        {point.name}
                      </span>
                    </div>
                    <div className="flex items-center gap-3 text-sm">
                      <span className="text-muted-foreground">{point.count} 问题</span>
                      <span className="flex items-center gap-1 text-green-500">
                        <TrendingUp className="h-3 w-3" />
                        {point.trend}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Sidebar - Quick Start */}
          <div className="space-y-6">
            {/* Quick Actions */}
            <div className="rounded-2xl border border-border/50 bg-card/50 p-6 backdrop-blur-sm">
              <h3 className="mb-4 text-lg font-semibold text-foreground">快速开始</h3>
              <div className="grid grid-cols-2 gap-3">
                {quickActions.map((action) => (
                  <button
                    key={action.label}
                    className="group flex flex-col items-center gap-2 rounded-xl border border-transparent bg-muted/30 p-4 transition-all duration-300 hover:border-[#667eea]/30 hover:bg-[#667eea]/5"
                  >
                    <span className="text-2xl transition-transform duration-300 group-hover:scale-110">
                      {action.icon}
                    </span>
                    <span className="font-medium text-foreground">{action.label}</span>
                    <span className="text-xs text-muted-foreground">{action.desc}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Promotion Card */}
            <div className="group relative overflow-hidden rounded-2xl bg-gradient-to-br from-[#667eea] to-[#764ba2] p-6 text-white">
              <div className="relative z-10">
                <div className="mb-2 text-sm font-medium opacity-90">AI 学习助手</div>
                <h3 className="mb-3 text-xl font-bold">智能问答全面升级</h3>
                <p className="mb-4 text-sm leading-relaxed opacity-80">
                  支持代码解释、知识图谱关联、个性化学习建议
                </p>
                <button className="rounded-lg bg-white/20 px-4 py-2 text-sm font-medium backdrop-blur-sm transition-all duration-300 hover:bg-white/30">
                  立即体验
                </button>
              </div>
              {/* Decorative */}
              <div className="absolute -bottom-10 -right-10 h-40 w-40 rounded-full bg-white/10 transition-transform duration-500 group-hover:scale-110" />
              <div className="absolute -top-5 right-5 h-20 w-20 rounded-full bg-white/5" />
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
