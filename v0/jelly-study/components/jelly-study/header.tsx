"use client"

import { useState } from "react"
import Link from "next/link"
import { Menu, X, BookOpen, MessageSquare, BarChart3, Lightbulb, Home } from "lucide-react"
import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

const navItems = [
  { name: "首页", href: "/", icon: Home },
  { name: "问题列表", href: "/question/list", icon: BookOpen },
  { name: "提问", href: "/question/create", icon: MessageSquare },
  { name: "知识点管理", href: "/knowledge/list", icon: Lightbulb },
  { name: "统计信息", href: "/question/statistics", icon: BarChart3 },
]

export default function Header() {
  const [isOpen, setIsOpen] = useState(false)

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border/40 bg-background/80 backdrop-blur-xl supports-[backdrop-filter]:bg-background/60">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-3 group">
          <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-[#667eea] to-[#764ba2] shadow-lg transition-transform duration-300 group-hover:scale-110">
            <span className="text-xl font-bold text-white">J</span>
            <div className="absolute inset-0 rounded-xl bg-gradient-to-br from-[#667eea] to-[#764ba2] opacity-0 blur-xl transition-opacity duration-300 group-hover:opacity-50" />
          </div>
          <div className="flex flex-col">
            <span className="bg-gradient-to-r from-[#667eea] to-[#764ba2] bg-clip-text text-lg font-bold text-transparent">
              JellyStudy
            </span>
            <span className="text-[10px] text-muted-foreground">博识尊</span>
          </div>
        </Link>

        {/* Desktop Nav */}
        <nav className="hidden items-center gap-1 md:flex">
          {navItems.map((item) => (
            <Link
              key={item.name}
              href={item.href}
              className="group relative flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium text-muted-foreground transition-all duration-300 hover:text-foreground"
            >
              <item.icon className="h-4 w-4 transition-transform duration-300 group-hover:scale-110" />
              {item.name}
              <span className="absolute inset-x-0 -bottom-px h-px scale-x-0 bg-gradient-to-r from-[#667eea] to-[#764ba2] transition-transform duration-300 group-hover:scale-x-100" />
            </Link>
          ))}
        </nav>

        {/* Actions */}
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            className="hidden border-border/50 hover:border-[#667eea]/50 hover:bg-[#667eea]/10 sm:flex"
          >
            登录
          </Button>
          <Button
            size="sm"
            className="bg-gradient-to-r from-[#667eea] to-[#764ba2] text-white hover:opacity-90"
          >
            开始使用
          </Button>

          {/* Mobile Menu Button */}
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            onClick={() => setIsOpen(!isOpen)}
          >
            {isOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </Button>
        </div>
      </div>

      {/* Mobile Nav */}
      <div
        className={cn(
          "overflow-hidden border-t border-border/40 bg-background/95 backdrop-blur-xl transition-all duration-300 md:hidden",
          isOpen ? "max-h-96" : "max-h-0"
        )}
      >
        <nav className="flex flex-col p-4">
          {navItems.map((item) => (
            <Link
              key={item.name}
              href={item.href}
              onClick={() => setIsOpen(false)}
              className="flex items-center gap-3 rounded-lg px-4 py-3 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
            >
              <item.icon className="h-5 w-5" />
              {item.name}
            </Link>
          ))}
        </nav>
      </div>
    </header>
  )
}
