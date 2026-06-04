"use client"

import { Eye, Heart, MessageSquare, ChevronRight, Clock, User } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"

interface Question {
  id: number
  title: string
  author: string
  time: string
  views: number
  likes: number
  comments: number
  tags: string[]
  isHot?: boolean
}

const latestQuestions: Question[] = [
  {
    id: 1,
    title: "如何理解动态规划中的状态转移方程？",
    author: "张三",
    time: "10分钟前",
    views: 128,
    likes: 24,
    comments: 8,
    tags: ["算法", "动态规划"],
    isHot: true,
  },
  {
    id: 2,
    title: "React Hooks 的闭包陷阱该如何避免？",
    author: "李四",
    time: "30分钟前",
    views: 89,
    likes: 15,
    comments: 5,
    tags: ["React", "前端"],
  },
  {
    id: 3,
    title: "深入理解 Python 装饰器的原理",
    author: "王五",
    time: "1小时前",
    views: 156,
    likes: 32,
    comments: 12,
    tags: ["Python", "进阶"],
  },
  {
    id: 4,
    title: "数据库索引的底层实现原理是什么？",
    author: "赵六",
    time: "2小时前",
    views: 203,
    likes: 45,
    comments: 18,
    tags: ["数据库", "MySQL"],
  },
]

const hotQuestions: Question[] = [
  {
    id: 5,
    title: "微服务架构中如何处理分布式事务？",
    author: "陈七",
    time: "3小时前",
    views: 512,
    likes: 89,
    comments: 34,
    tags: ["微服务", "分布式"],
    isHot: true,
  },
  {
    id: 6,
    title: "机器学习入门：如何选择第一个模型？",
    author: "周八",
    time: "5小时前",
    views: 387,
    likes: 67,
    comments: 28,
    tags: ["机器学习", "AI"],
    isHot: true,
  },
  {
    id: 7,
    title: "Git 高级技巧：rebase vs merge 的选择",
    author: "吴九",
    time: "昨天",
    views: 445,
    likes: 78,
    comments: 25,
    tags: ["Git", "工具"],
  },
]

function QuestionItem({ question, index }: { question: Question; index: number }) {
  return (
    <div
      className={cn(
        "group relative cursor-pointer rounded-xl border border-transparent p-4 transition-all duration-300",
        "hover:border-[#667eea]/30 hover:bg-gradient-to-r hover:from-[#667eea]/5 hover:to-transparent"
      )}
      style={{ animationDelay: `${index * 0.1}s` }}
    >
      <div className="flex items-start gap-4">
        {/* Hot Badge */}
        {question.isHot && (
          <div className="absolute -right-1 -top-1 flex h-6 w-6 items-center justify-center rounded-full bg-gradient-to-r from-[#ff6b6b] to-[#ee5a5a] text-[10px] font-bold text-white shadow-lg">
            热
          </div>
        )}

        {/* Content */}
        <div className="flex-1 space-y-3">
          {/* Title */}
          <h3 className="line-clamp-2 text-base font-medium text-foreground transition-colors group-hover:text-[#667eea]">
            {question.title}
          </h3>

          {/* Tags */}
          <div className="flex flex-wrap gap-2">
            {question.tags.map((tag) => (
              <Badge
                key={tag}
                variant="secondary"
                className="border-0 bg-[#667eea]/10 text-[#667eea] hover:bg-[#667eea]/20"
              >
                {tag}
              </Badge>
            ))}
          </div>

          {/* Meta */}
          <div className="flex flex-wrap items-center gap-4 text-xs text-muted-foreground">
            <span className="flex items-center gap-1">
              <User className="h-3 w-3" />
              {question.author}
            </span>
            <span className="flex items-center gap-1">
              <Clock className="h-3 w-3" />
              {question.time}
            </span>
            <span className="flex items-center gap-1">
              <Eye className="h-3 w-3" />
              {question.views}
            </span>
            <span className="flex items-center gap-1">
              <Heart className="h-3 w-3" />
              {question.likes}
            </span>
            <span className="flex items-center gap-1">
              <MessageSquare className="h-3 w-3" />
              {question.comments}
            </span>
          </div>
        </div>

        {/* Arrow */}
        <ChevronRight className="h-5 w-5 text-muted-foreground/50 transition-all duration-300 group-hover:translate-x-1 group-hover:text-[#667eea]" />
      </div>
    </div>
  )
}

interface QuestionCardProps {
  title: string
  questions: Question[]
  accentColor?: string
}

function QuestionCard({ title, questions, accentColor = "#667eea" }: QuestionCardProps) {
  return (
    <div className="group relative overflow-hidden rounded-2xl border border-border/50 bg-card/50 backdrop-blur-sm transition-all duration-500 hover:border-[#667eea]/30 hover:shadow-xl hover:shadow-[#667eea]/5">
      {/* Header */}
      <div className="border-b border-border/50 p-5">
        <h2 className="flex items-center gap-3 text-lg font-semibold text-foreground">
          <div 
            className="h-1.5 w-1.5 rounded-full"
            style={{ backgroundColor: accentColor }}
          />
          {title}
        </h2>
      </div>

      {/* Content */}
      <div className="divide-y divide-border/30 p-2">
        {questions.map((question, index) => (
          <QuestionItem key={question.id} question={question} index={index} />
        ))}
      </div>

      {/* Footer */}
      <div className="border-t border-border/50 p-4">
        <button className="group/btn flex w-full items-center justify-center gap-2 rounded-lg py-2 text-sm text-muted-foreground transition-all duration-300 hover:text-[#667eea]">
          查看全部
          <ChevronRight className="h-4 w-4 transition-transform duration-300 group-hover/btn:translate-x-1" />
        </button>
      </div>
    </div>
  )
}

export default function QuestionsSection() {
  return (
    <section className="py-16">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="mb-10 text-center">
          <h2 className="text-2xl font-bold text-foreground sm:text-3xl">
            探索<span className="bg-gradient-to-r from-[#667eea] to-[#764ba2] bg-clip-text text-transparent">知识</span>
          </h2>
          <p className="mt-2 text-muted-foreground">发现最新和最热门的问题</p>
        </div>

        {/* Grid */}
        <div className="grid gap-6 lg:grid-cols-2">
          <QuestionCard title="最新问题" questions={latestQuestions} />
          <QuestionCard title="热门问题" questions={hotQuestions} accentColor="#764ba2" />
        </div>
      </div>
    </section>
  )
}
