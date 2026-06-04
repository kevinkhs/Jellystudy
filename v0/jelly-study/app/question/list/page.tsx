'use client'

import { useState } from 'react'
import Link from 'next/link'
import Header from '@/components/jelly-study/header'
import Footer from '@/components/jelly-study/footer'

interface Question {
  id: number
  title: string
  content: string
  author: string
  knowledgePointTitle: string
  createTime: string
  viewCount: number
  answerCount: number
  likeCount: number
}

const mockQuestions: Question[] = [
  {
    id: 1,
    title: '如何在 React 中使用 useContext 进行状态管理？',
    content: '我想在组件间共享状态，有什么最佳实践吗？',
    author: '开发小王',
    knowledgePointTitle: 'React Hooks',
    createTime: '2024-06-03 14:30',
    viewCount: 245,
    answerCount: 3,
    likeCount: 12,
  },
  {
    id: 2,
    title: 'Next.js 中 SSR 和 SSG 的性能差异',
    content: '在实际项目中应该如何选择合适的渲染方式？',
    author: '前端小李',
    knowledgePointTitle: 'Next.js',
    createTime: '2024-06-02 10:15',
    viewCount: 189,
    answerCount: 5,
    likeCount: 24,
  },
  {
    id: 3,
    title: 'TypeScript 中的类型推断机制详解',
    content: '如何利用类型推断来编写更简洁的代码？',
    author: '全栈工程师',
    knowledgePointTitle: 'TypeScript',
    createTime: '2024-06-01 16:45',
    viewCount: 312,
    answerCount: 2,
    likeCount: 18,
  },
]

export default function QuestionListPage() {
  const [questions] = useState<Question[]>(mockQuestions)
  const [filterTag, setFilterTag] = useState<string>('')

  const filteredQuestions = filterTag
    ? questions.filter(q => q.knowledgePointTitle === filterTag)
    : questions

  const tags = Array.from(new Set(questions.map(q => q.knowledgePointTitle)))

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <Header />
      
      <main className="flex-1">
        {/* Hero 区域 */}
        <section className="bg-gradient-primary py-12">
          <div className="container mx-auto px-4">
            <div className="animate-slide-up">
              <h1 className="text-4xl font-bold text-white mb-4 text-balance">问题列表</h1>
              <p className="text-lg text-white/80">汇聚智慧，解答疑惑 - 浏览社区中的热点问题</p>
            </div>
          </div>
        </section>

        {/* 内容区域 */}
        <section className="py-12">
          <div className="container mx-auto px-4">
            <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
              {/* 侧边栏 */}
              <aside className="lg:col-span-1">
                <div className="sticky top-20">
                  <div className="bg-card rounded-xl p-6 border border-border shadow-lg">
                    <h3 className="font-bold text-lg text-foreground mb-4">筛选标签</h3>
                    
                    <button
                      onClick={() => setFilterTag('')}
                      className={`w-full text-left px-4 py-2 rounded-lg mb-2 transition-all ${
                        filterTag === ''
                          ? 'bg-primary text-primary-foreground'
                          : 'bg-muted text-muted-foreground hover:bg-muted/80'
                      }`}
                    >
                      全部问题
                    </button>

                    {tags.map(tag => (
                      <button
                        key={tag}
                        onClick={() => setFilterTag(tag)}
                        className={`w-full text-left px-4 py-2 rounded-lg mb-2 transition-all ${
                          filterTag === tag
                            ? 'bg-primary text-primary-foreground'
                            : 'bg-muted text-muted-foreground hover:bg-muted/80'
                        }`}
                      >
                        {tag}
                      </button>
                    ))}
                  </div>

                  <Link
                    href="/question/create"
                    className="block w-full mt-6 bg-gradient-primary text-white font-bold py-3 px-6 rounded-xl text-center hover:shadow-lg transition-shadow"
                  >
                    + 提出问题
                  </Link>
                </div>
              </aside>

              {/* 问题列表 */}
              <div className="lg:col-span-3">
                {filteredQuestions.length === 0 ? (
                  <div className="bg-card rounded-xl p-12 text-center border border-border">
                    <p className="text-muted-foreground text-lg">暂无相关问题</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {filteredQuestions.map((question, index) => (
                      <Link
                        key={question.id}
                        href={`/question/${question.id}`}
                        className="animate-slide-up"
                        style={{ animationDelay: `${index * 0.1}s` }}
                      >
                        <div className="bg-card rounded-xl p-6 border border-border hover:border-primary transition-all hover:shadow-lg hover:-translate-y-1 cursor-pointer group">
                          <div className="flex justify-between items-start gap-4 mb-3">
                            <h3 className="text-xl font-bold text-foreground group-hover:text-primary transition-colors line-clamp-2">
                              {question.title}
                            </h3>
                            <span className="inline-block bg-accent/10 text-accent px-3 py-1 rounded-full text-sm font-medium whitespace-nowrap">
                              {question.knowledgePointTitle}
                            </span>
                          </div>

                          <p className="text-muted-foreground mb-4 line-clamp-2">
                            {question.content}
                          </p>

                          <div className="flex flex-wrap gap-4 text-sm text-muted-foreground">
                            <span>👤 {question.author}</span>
                            <span>📅 {question.createTime}</span>
                            <span>👁️ {question.viewCount} 浏览</span>
                            <span>💬 {question.answerCount} 回答</span>
                            <span className="text-red-500 font-semibold">❤️ {question.likeCount}</span>
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                )}

                {/* 分页 */}
                <div className="flex justify-center gap-2 mt-12">
                  <button className="px-4 py-2 rounded-lg border border-border bg-card text-foreground hover:bg-muted transition-colors disabled:opacity-50">
                    ← 上一页
                  </button>
                  {[1, 2, 3, 4, 5].map(page => (
                    <button
                      key={page}
                      className={`px-4 py-2 rounded-lg transition-colors ${
                        page === 1
                          ? 'bg-primary text-primary-foreground'
                          : 'border border-border bg-card text-foreground hover:bg-muted'
                      }`}
                    >
                      {page}
                    </button>
                  ))}
                  <button className="px-4 py-2 rounded-lg border border-border bg-card text-foreground hover:bg-muted transition-colors">
                    下一页 →
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  )
}
