"use client"

import { ArrowRight, Sparkles, Brain, Target, Zap } from "lucide-react"
import { Button } from "@/components/ui/button"

export default function HeroSection() {
  return (
    <section className="relative overflow-hidden py-20 lg:py-32">
      {/* Background Effects */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -left-40 -top-40 h-[500px] w-[500px] rounded-full bg-[#667eea]/20 blur-[120px] animate-float" />
        <div className="absolute -bottom-40 -right-40 h-[500px] w-[500px] rounded-full bg-[#764ba2]/20 blur-[120px] animate-float" style={{ animationDelay: '3s' }} />
        <div className="absolute left-1/2 top-1/2 h-[300px] w-[300px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-[#667eea]/10 blur-[80px]" />
      </div>

      <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col items-center text-center">
          {/* Badge */}
          <div className="mb-8 inline-flex animate-slide-up items-center gap-2 rounded-full border border-[#667eea]/30 bg-[#667eea]/10 px-4 py-2 text-sm">
            <Sparkles className="h-4 w-4 text-[#667eea]" />
            <span className="text-muted-foreground">AI 驱动的智能学习平台</span>
          </div>

          {/* Main Title */}
          <h1 className="animate-slide-up stagger-1 max-w-4xl text-balance text-4xl font-bold tracking-tight sm:text-5xl lg:text-6xl">
            <span className="text-foreground">让知识像</span>
            <span className="bg-gradient-to-r from-[#667eea] via-[#764ba2] to-[#667eea] bg-[length:200%_auto] bg-clip-text text-transparent animate-gradient">
              果冻
            </span>
            <span className="text-foreground">一样</span>
            <br />
            <span className="text-foreground">清晰又通透</span>
          </h1>

          {/* Subtitle */}
          <p className="mt-6 max-w-2xl animate-slide-up stagger-2 text-pretty text-lg leading-relaxed text-muted-foreground sm:text-xl">
            JellyStudy 博识尊 — 不只是题库，而是你的 AI 学习伙伴。
            通过智能问答、知识图谱和个性化推荐，让学习更高效、更有趣。
          </p>

          {/* CTA Buttons */}
          <div className="mt-10 flex animate-slide-up stagger-3 flex-col gap-4 sm:flex-row">
            <Button
              size="lg"
              className="group gap-2 bg-gradient-to-r from-[#667eea] to-[#764ba2] text-lg text-white hover:opacity-90"
            >
              开始探索
              <ArrowRight className="h-5 w-5 transition-transform duration-300 group-hover:translate-x-1" />
            </Button>
            <Button
              size="lg"
              variant="outline"
              className="border-border/50 text-lg hover:border-[#667eea]/50 hover:bg-[#667eea]/10"
            >
              了解更多
            </Button>
          </div>

          {/* Feature Pills */}
          <div className="mt-16 flex animate-slide-up stagger-4 flex-wrap items-center justify-center gap-4">
            {[
              { icon: Brain, text: "AI 智能问答" },
              { icon: Target, text: "知识图谱可视化" },
              { icon: Zap, text: "个性化学习路径" },
            ].map((feature) => (
              <div
                key={feature.text}
                className="flex items-center gap-2 rounded-full border border-border/50 bg-card/50 px-4 py-2 text-sm transition-all duration-300 hover:border-[#667eea]/50 hover:bg-[#667eea]/5"
              >
                <feature.icon className="h-4 w-4 text-[#667eea]" />
                <span className="text-muted-foreground">{feature.text}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}
