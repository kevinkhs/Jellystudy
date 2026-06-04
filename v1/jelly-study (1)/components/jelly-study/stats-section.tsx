"use client"

import { BookOpen, Users, MessageSquare, Award } from "lucide-react"

const stats = [
  { label: "总问题数", value: "2,847", icon: BookOpen, color: "from-[#667eea] to-[#764ba2]" },
  { label: "活跃用户", value: "1,234", icon: Users, color: "from-[#764ba2] to-[#667eea]" },
  { label: "AI 对话", value: "15,678", icon: MessageSquare, color: "from-[#667eea] to-[#28a745]" },
  { label: "知识点", value: "486", icon: Award, color: "from-[#764ba2] to-[#667eea]" },
]

export default function StatsSection() {
  return (
    <section className="relative py-16">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 gap-4 sm:gap-6 lg:grid-cols-4">
          {stats.map((stat, index) => (
            <div
              key={stat.label}
              className="group relative overflow-hidden rounded-2xl border border-border/50 bg-card/50 p-6 backdrop-blur-sm transition-all duration-500 hover:border-[#667eea]/50 hover:shadow-lg hover:shadow-[#667eea]/10"
              style={{ animationDelay: `${index * 0.1}s` }}
            >
              {/* Gradient Background on Hover */}
              <div className={`absolute inset-0 bg-gradient-to-br ${stat.color} opacity-0 transition-opacity duration-500 group-hover:opacity-5`} />
              
              {/* Icon */}
              <div className={`mb-4 inline-flex rounded-xl bg-gradient-to-br ${stat.color} p-3 shadow-lg`}>
                <stat.icon className="h-6 w-6 text-white" />
              </div>
              
              {/* Value */}
              <div className="text-3xl font-bold text-foreground transition-transform duration-300 group-hover:scale-105 sm:text-4xl">
                {stat.value}
              </div>
              
              {/* Label */}
              <div className="mt-1 text-sm text-muted-foreground">
                {stat.label}
              </div>
              
              {/* Decorative Element */}
              <div className="absolute -bottom-2 -right-2 h-20 w-20 rounded-full bg-gradient-to-br from-[#667eea]/10 to-transparent blur-2xl transition-all duration-500 group-hover:h-24 group-hover:w-24" />
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
