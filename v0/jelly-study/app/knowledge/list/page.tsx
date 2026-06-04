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
  questionCount: number
  difficulty: 'easy' | 'medium' | 'hard'
  color: string
}

const mockKnowledgePoints: KnowledgePoint[] = [
  {
    id: 1,
    title: 'React Hooks',
    category: 'Frontend',
    description: '深入理解 React Hooks 的原理和最佳实践，包括 useState、useEffect、useContext 等。',
    questionCount: 45,
    difficulty: 'medium',
    color: 'from-blue-500 to-blue-600',
  },
  {
    id: 2,
    title: 'Next.js 服务器组件',
    category: 'Frontend',
    description: '学习 Next.js 最新的服务器组件特性，提高应用性能。',
    questionCount: 32,
    difficulty: 'medium',
    color: 'from-indigo-500 to-indigo-600',
  },
  {
    id: 3,
    title: 'TypeScript 高级类型',
    category: 'Language',
    description: '掌握 TypeScript 的高级类型系统，编写类型安全的代码。',
    questionCount: 28,
    difficulty: 'hard',
    color: 'from-cyan-500 to-cyan-600',
  },
  {
    id: 4,
    title: 'Node.js 异步编程',
    category: 'Backend',
    description: '理解 JavaScript 异步编程的核心概念，包括 Promise、async/await。',
    questionCount: 52,
    difficulty: 'medium',
    color: 'from-green-500 to-green-600',
  },
  {
    id: 5,
    title: '数据库设计',
    category: 'Backend',
    description: '学习关系型数据库设计原理，包括范式、索引、优化等。',
    questionCount: 38,
    difficulty: 'hard',
    color: 'from-orange-500 to-orange-600',
  },
  {
    id: 6,
    title: 'Web 性能优化',
    category: 'Performance',
    description: '优化网站性能的各种技术和工具，提升用户体验。',
    questionCount: 41,
    difficulty: 'medium',
    color: 'from-red-500 to-red-600',
  },
]

export default function KnowledgeListPage() {
  const [knowledgePoints] = useState<KnowledgePoint[]>(mockKnowledgePoints)
  const [filterCategory, setFilterCategory] = useState<string>('')
  const [searchQuery, setSearchQuery] = useState('')

  const categories = Array.from(new Set(knowledgePoints.map(k => k.category)))

  const filteredPoints = knowledgePoints.filter(k => {
    const matchCategory = !filterCategory || k.category === filterCategory
    const matchSearch = !searchQuery || k.title.includes(searchQuery) || k.description.includes(searchQuery)
    return matchCategory && matchSearch
  })

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <Header />

      <main className="flex-1">
        {/* Hero 区域 */}
        <section className="bg-gradient-primary py-12">
          <div className="container mx-auto px-4">
            <div className="animate-slide-up">
              <h1 className="text-4xl font-bold text-white mb-4 text-balance">知识点库</h1>
              <p className="text-lg text-white/80">系统地学习各个领域的知识点，提升你的技能</p>
            </div>
          </div>
        </section>

        {/* 筛选和搜索 */}
        <section className="bg-card border-b border-border py-6">
          <div className="container mx-auto px-4">
            <div className="flex flex-col md:flex-row gap-4 md:items-center md:justify-between">
              {/* 搜索框 */}
              <div className="flex-1">
                <input
                  type="text"
                  placeholder="搜索知识点..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full bg-background border border-border rounded-lg px-4 py-2 text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>

              {/* 分类筛选 */}
              <div className="flex gap-2 overflow-x-auto">
                <button
                  onClick={() => setFilterCategory('')}
                  className={`px-4 py-2 rounded-lg whitespace-nowrap font-medium transition-all ${
                    filterCategory === ''
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-muted text-muted-foreground hover:bg-muted/80'
                  }`}
                >
                  全部
                </button>
                {categories.map(cat => (
                  <button
                    key={cat}
                    onClick={() => setFilterCategory(cat)}
                    className={`px-4 py-2 rounded-lg whitespace-nowrap font-medium transition-all ${
                      filterCategory === cat
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-muted text-muted-foreground hover:bg-muted/80'
                    }`}
                  >
                    {cat}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </section>

        {/* 知识点网格 */}
        <section className="py-12">
          <div className="container mx-auto px-4">
            {filteredPoints.length === 0 ? (
              <div className="text-center py-12">
                <p className="text-muted-foreground text-lg">没有找到匹配的知识点</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredPoints.map((kp, index) => (
                  <Link
                    key={kp.id}
                    href={`/knowledge/${kp.id}`}
                    className="group animate-slide-up"
                    style={{ animationDelay: `${index * 0.1}s` }}
                  >
                    <div className="bg-card rounded-xl overflow-hidden border border-border hover:border-primary transition-all hover:shadow-lg hover:-translate-y-1 cursor-pointer h-full">
                      {/* 顶部渐变条 */}
                      <div className={`h-2 bg-gradient-to-r ${kp.color}`} />

                      <div className="p-6">
                        <div className="flex items-start justify-between mb-3">
                          <h3 className="text-xl font-bold text-foreground group-hover:text-primary transition-colors">
                            {kp.title}
                          </h3>
                          <span className="text-2xl">📚</span>
                        </div>

                        <span className="inline-block bg-accent/10 text-accent px-3 py-1 rounded-full text-sm font-medium mb-4">
                          {kp.category}
                        </span>

                        <p className="text-muted-foreground mb-6 line-clamp-3">
                          {kp.description}
                        </p>

                        <div className="flex items-center justify-between pt-4 border-t border-border">
                          <div className="flex gap-4 text-sm text-muted-foreground">
                            <span>💬 {kp.questionCount} 个问题</span>
                            <span className={
                              kp.difficulty === 'easy' ? 'text-success' :
                              kp.difficulty === 'medium' ? 'text-yellow-500' :
                              'text-destructive'
                            }>
                              难度: {kp.difficulty === 'easy' ? '简单' : kp.difficulty === 'medium' ? '中等' : '困难'}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </section>

        {/* 创建知识点 CTA */}
        <section className="py-12 bg-card border-t border-border">
          <div className="container mx-auto px-4 text-center">
            <h2 className="text-3xl font-bold text-foreground mb-4">想要分享你的知识？</h2>
            <p className="text-muted-foreground mb-8 text-lg">创建新的知识点，帮助更多的学习者</p>
            <Link
              href="/knowledge/create"
              className="inline-block bg-gradient-primary text-white font-bold py-3 px-8 rounded-xl hover:shadow-lg transition-shadow text-lg"
            >
              + 创建知识点
            </Link>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  )
}
