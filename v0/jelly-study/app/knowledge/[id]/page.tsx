'use client'

import { useState } from 'react'
import Link from 'next/link'
import Header from '@/components/jelly-study/header'
import Footer from '@/components/jelly-study/footer'

interface KnowledgePoint {
  id: number
  title: string
  category: string
  description: string
  content: string
  questionCount: number
  relatedTopics: string[]
  createdBy: string
  createdAt: string
}

const mockKnowledgePoint: KnowledgePoint = {
  id: 1,
  title: 'React Hooks',
  category: 'Frontend',
  description: '深入理解 React Hooks 的原理和最佳实践',
  content: `React Hooks 是 React 16.8 引入的特性，允许你在不编写类的情况下使用状态和其他 React 特性。

## 核心 Hooks

### useState
用于在函数组件中添加状态。每个 Hook 调用都会返回一对值：当前的状态值和一个让你更新它的函数。

### useEffect
用于在函数组件中执行副作用。它服从与 componentDidMount、componentDidUpdate 和 componentWillUnmount 三个函数相同的用途。

### useContext
让你订阅 React 的 Context，而不必引入嵌套的消费者。

### useReducer
useState 的替代方案，用于处理复杂的状态逻辑。

## 最佳实践

1. 只在最顶层调用 Hooks
2. 只在 React 函数中调用 Hooks
3. 使用 ESLint 插件来强制执行这些规则
4. 合理拆分自定义 Hooks 以提高代码复用性`,
  questionCount: 45,
  relatedTopics: ['React Basics', 'JavaScript ES6', 'Functional Programming'],
  createdBy: '前端专家',
  createdAt: '2024-05-15',
}

export default function KnowledgeDetailPage() {
  const [knowledge] = useState<KnowledgePoint>(mockKnowledgePoint)
  const [liked, setLiked] = useState(false)

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <Header />

      <main className="flex-1">
        {/* Hero 区域 */}
        <section className="bg-gradient-primary py-12">
          <div className="container mx-auto px-4">
            <Link href="/knowledge/list" className="text-white/70 hover:text-white transition-colors mb-4 inline-block">
              ← 返回知识点库
            </Link>
            <h1 className="text-4xl font-bold text-white mb-2">{knowledge.title}</h1>
            <p className="text-white/80">{knowledge.description}</p>
          </div>
        </section>

        {/* 内容区域 */}
        <section className="py-12">
          <div className="container mx-auto px-4">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              {/* 主内容 */}
              <div className="lg:col-span-2">
                {/* 知识点卡片 */}
                <div className="bg-card rounded-xl p-8 border border-border mb-8 shadow-lg animate-slide-up">
                  <div className="flex flex-wrap gap-3 mb-8">
                    <span className="bg-accent/10 text-accent px-4 py-1.5 rounded-full text-sm font-medium">
                      {knowledge.category}
                    </span>
                    <span className="text-sm text-muted-foreground">
                      创建者: {knowledge.createdBy}
                    </span>
                    <span className="text-sm text-muted-foreground">
                      创建时间: {knowledge.createdAt}
                    </span>
                  </div>

                  <div className="prose prose-invert max-w-none">
                    {knowledge.content.split('\n\n').map((paragraph, idx) => {
                      if (paragraph.startsWith('##')) {
                        return (
                          <h3 key={idx} className="text-2xl font-bold text-foreground mt-8 mb-4">
                            {paragraph.replace('##', '').trim()}
                          </h3>
                        )
                      }
                      if (paragraph.startsWith('###')) {
                        return (
                          <h4 key={idx} className="text-xl font-bold text-foreground mt-6 mb-3">
                            {paragraph.replace('###', '').trim()}
                          </h4>
                        )
                      }
                      if (paragraph.startsWith('1.') || paragraph.startsWith('2.') || paragraph.startsWith('3.')) {
                        return (
                          <ol key={idx} className="list-decimal list-inside text-foreground space-y-2 my-4">
                            {paragraph.split('\n').map((item, i) => (
                              <li key={i} className="text-foreground/80">{item.replace(/^[\d.]\s*/, '')}</li>
                            ))}
                          </ol>
                        )
                      }
                      return (
                        <p key={idx} className="text-foreground/80 leading-relaxed">
                          {paragraph}
                        </p>
                      )
                    })}
                  </div>
                </div>

                {/* 相关问题 */}
                <div className="mb-8">
                  <h3 className="text-2xl font-bold text-foreground mb-6">
                    💬 相关问题 ({knowledge.questionCount})
                  </h3>

                  <div className="space-y-4">
                    {[1, 2, 3].map((i) => (
                      <Link
                        key={i}
                        href={`/question/${i}`}
                        className="block bg-card rounded-xl p-6 border border-border hover:border-primary transition-all hover:shadow-lg group"
                      >
                        <h4 className="font-bold text-foreground group-hover:text-primary transition-colors mb-2">
                          示例问题 {i}: 关于 {knowledge.title} 的实践问题
                        </h4>
                        <p className="text-sm text-muted-foreground">👤 提问者 · 📅 最近回答</p>
                      </Link>
                    ))}
                  </div>

                  <Link
                    href="/question/list"
                    className="block mt-6 text-center text-primary hover:text-primary/80 font-bold transition-colors"
                  >
                    查看所有相关问题 →
                  </Link>
                </div>
              </div>

              {/* 侧边栏 */}
              <aside className="lg:col-span-1">
                <div className="sticky top-20 space-y-6">
                  {/* 操作按钮 */}
                  <button
                    onClick={() => setLiked(!liked)}
                    className={`w-full py-3 px-6 rounded-xl font-bold transition-all ${
                      liked
                        ? 'bg-destructive text-destructive-foreground'
                        : 'bg-primary text-primary-foreground hover:shadow-lg'
                    }`}
                  >
                    {liked ? '❤️ 已赞' : '🤍 点赞'}
                  </button>

                  <button className="w-full bg-secondary text-secondary-foreground py-3 px-6 rounded-xl font-bold hover:shadow-lg transition-all">
                    🔖 收藏
                  </button>

                  <Link
                    href={`/knowledge/1/edit`}
                    className="block w-full bg-accent text-accent-foreground py-3 px-6 rounded-xl font-bold hover:shadow-lg transition-all text-center"
                  >
                    ✏️ 编辑
                  </Link>

                  {/* 相关主题 */}
                  <div className="bg-card rounded-xl p-6 border border-border">
                    <h4 className="font-bold text-foreground mb-4">🏷️ 相关主题</h4>
                    <div className="space-y-2">
                      {knowledge.relatedTopics.map((topic, idx) => (
                        <Link
                          key={idx}
                          href="#"
                          className="block text-accent hover:text-accent/80 transition-colors text-sm"
                        >
                          → {topic}
                        </Link>
                      ))}
                    </div>
                  </div>

                  {/* 统计信息 */}
                  <div className="bg-card rounded-xl p-6 border border-border">
                    <h4 className="font-bold text-foreground mb-4">📊 统计信息</h4>
                    <div className="space-y-3 text-sm">
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">相关问题数</span>
                        <span className="font-bold text-foreground">{knowledge.questionCount}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">浏览次数</span>
                        <span className="font-bold text-foreground">1,234</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">收藏次数</span>
                        <span className="font-bold text-foreground">89</span>
                      </div>
                    </div>
                  </div>
                </div>
              </aside>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  )
}
