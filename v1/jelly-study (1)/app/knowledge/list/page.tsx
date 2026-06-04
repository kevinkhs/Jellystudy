'use client'

import { useState, useCallback } from 'react'
import Link from 'next/link'
import Header from '@/components/jelly-study/header'
import Footer from '@/components/jelly-study/footer'
import { Sparkles, Star, Zap, Crown, BookOpen, ChevronRight, RotateCcw } from 'lucide-react'

type Rarity = 'common' | 'rare' | 'epic' | 'legendary'

interface KnowledgeCard {
  id: number
  title: string
  category: string
  description: string
  questionCount: number
  rarity: Rarity
  level: number
  collected: boolean
  icon: string
}

const rarityConfig = {
  common: {
    label: '普通',
    color: 'from-slate-400 to-slate-500',
    bgGlow: 'shadow-slate-500/30',
    borderColor: 'border-slate-500/50',
    textColor: 'text-slate-300',
    stars: 1,
  },
  rare: {
    label: '稀有',
    color: 'from-blue-400 to-blue-600',
    bgGlow: 'shadow-blue-500/40',
    borderColor: 'border-blue-500/50',
    textColor: 'text-blue-400',
    stars: 2,
  },
  epic: {
    label: '史诗',
    color: 'from-purple-400 to-purple-600',
    bgGlow: 'shadow-purple-500/50',
    borderColor: 'border-purple-500/50',
    textColor: 'text-purple-400',
    stars: 3,
  },
  legendary: {
    label: '传说',
    color: 'from-amber-400 via-orange-500 to-red-500',
    bgGlow: 'shadow-amber-500/60',
    borderColor: 'border-amber-500/50',
    textColor: 'text-amber-400',
    stars: 4,
  },
}

const mockCards: KnowledgeCard[] = [
  {
    id: 1,
    title: 'React Hooks',
    category: 'Frontend',
    description: '深入理解 React Hooks 的原理和最佳实践',
    questionCount: 45,
    rarity: 'epic',
    level: 3,
    collected: true,
    icon: '⚛️',
  },
  {
    id: 2,
    title: 'Next.js 服务器组件',
    category: 'Frontend',
    description: '学习 Next.js 最新的服务器组件特性',
    questionCount: 32,
    rarity: 'legendary',
    level: 5,
    collected: true,
    icon: '🚀',
  },
  {
    id: 3,
    title: 'TypeScript 高级类型',
    category: 'Language',
    description: '掌握 TypeScript 的高级类型系统',
    questionCount: 28,
    rarity: 'rare',
    level: 2,
    collected: true,
    icon: '📘',
  },
  {
    id: 4,
    title: 'Node.js 异步编程',
    category: 'Backend',
    description: '理解 JavaScript 异步编程的核心概念',
    questionCount: 52,
    rarity: 'common',
    level: 1,
    collected: true,
    icon: '🟢',
  },
  {
    id: 5,
    title: '数据库设计',
    category: 'Backend',
    description: '学习关系型数据库设计原理',
    questionCount: 38,
    rarity: 'epic',
    level: 4,
    collected: false,
    icon: '🗄️',
  },
  {
    id: 6,
    title: 'Web 性能优化',
    category: 'Performance',
    description: '优化网站性能的各种技术和工具',
    questionCount: 41,
    rarity: 'rare',
    level: 2,
    collected: false,
    icon: '⚡',
  },
  {
    id: 7,
    title: 'GraphQL 入门',
    category: 'API',
    description: '学习 GraphQL 查询语言和最佳实践',
    questionCount: 25,
    rarity: 'common',
    level: 1,
    collected: false,
    icon: '🔷',
  },
  {
    id: 8,
    title: 'Docker 容器化',
    category: 'DevOps',
    description: '掌握 Docker 容器技术和部署流程',
    questionCount: 36,
    rarity: 'legendary',
    level: 5,
    collected: false,
    icon: '🐳',
  },
]

function KnowledgeCardComponent({ 
  card, 
  isFlipped, 
  onFlip 
}: { 
  card: KnowledgeCard
  isFlipped: boolean
  onFlip: () => void 
}) {
  const config = rarityConfig[card.rarity]
  
  return (
    <div 
      className="group perspective-1000 cursor-pointer"
      onClick={onFlip}
    >
      <div 
        className={`relative w-full aspect-[3/4] transition-all duration-700 transform-style-3d ${
          isFlipped ? 'rotate-y-180' : ''
        }`}
      >
        {/* 卡牌正面 */}
        <div className={`absolute inset-0 backface-hidden rounded-2xl overflow-hidden border-2 ${config.borderColor} ${
          card.collected ? '' : 'grayscale opacity-60'
        }`}>
          {/* 发光效果 */}
          <div className={`absolute inset-0 bg-gradient-to-br ${config.color} opacity-20`} />
          <div className={`absolute inset-0 shadow-[inset_0_0_30px_rgba(255,255,255,0.1)]`} />
          
          {/* 卡牌背景 */}
          <div className="absolute inset-0 bg-card/95 backdrop-blur-sm" />
          
          {/* 顶部装饰线 */}
          <div className={`absolute top-0 left-0 right-0 h-1 bg-gradient-to-r ${config.color}`} />
          
          {/* 卡牌内容 */}
          <div className="relative h-full flex flex-col p-4">
            {/* 稀有度标签 */}
            <div className="flex items-center justify-between mb-3">
              <span className={`text-xs font-bold px-2 py-1 rounded-full bg-gradient-to-r ${config.color} text-white`}>
                {config.label}
              </span>
              <div className="flex gap-0.5">
                {Array.from({ length: config.stars }).map((_, i) => (
                  <Star key={i} className={`w-3 h-3 fill-current ${config.textColor}`} />
                ))}
              </div>
            </div>
            
            {/* 图标区域 */}
            <div className="flex-1 flex items-center justify-center">
              <div className={`w-20 h-20 rounded-xl bg-gradient-to-br ${config.color} flex items-center justify-center text-4xl shadow-lg ${config.bgGlow} shadow-2xl group-hover:scale-110 transition-transform duration-300`}>
                {card.icon}
              </div>
            </div>
            
            {/* 卡牌信息 */}
            <div className="mt-auto">
              <h3 className="text-lg font-bold text-foreground mb-1 line-clamp-1">{card.title}</h3>
              <p className="text-xs text-muted-foreground mb-2 line-clamp-2">{card.description}</p>
              
              <div className="flex items-center justify-between text-xs">
                <span className="text-muted-foreground">{card.category}</span>
                <span className={config.textColor}>Lv.{card.level}</span>
              </div>
            </div>
            
            {/* 未收集遮罩 */}
            {!card.collected && (
              <div className="absolute inset-0 bg-background/60 flex items-center justify-center rounded-2xl">
                <div className="text-center">
                  <div className="text-4xl mb-2">🔒</div>
                  <span className="text-sm text-muted-foreground">未收集</span>
                </div>
              </div>
            )}
          </div>
          
          {/* 悬停发光效果 */}
          <div className={`absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none rounded-2xl shadow-[0_0_40px_rgba(102,126,234,0.4)]`} />
        </div>
        
        {/* 卡牌背面 */}
        <div className="absolute inset-0 backface-hidden rotate-y-180 rounded-2xl overflow-hidden border-2 border-primary/30 bg-card">
          <div className="absolute inset-0 bg-gradient-to-br from-primary/10 to-secondary/10" />
          <div className="relative h-full flex flex-col items-center justify-center p-6 text-center">
            <BookOpen className="w-12 h-12 text-primary mb-4" />
            <h3 className="text-xl font-bold text-foreground mb-2">{card.title}</h3>
            <p className="text-sm text-muted-foreground mb-4">{card.description}</p>
            <div className="text-sm text-muted-foreground">
              <span>{card.questionCount} 个相关问题</span>
            </div>
            {card.collected && (
              <Link 
                href={`/knowledge/${card.id}`}
                className="mt-4 px-4 py-2 bg-gradient-to-r from-primary to-secondary text-white rounded-lg font-medium hover:opacity-90 transition-opacity flex items-center gap-2"
                onClick={(e) => e.stopPropagation()}
              >
                查看详情 <ChevronRight className="w-4 h-4" />
              </Link>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

function GachaAnimation({ 
  isPlaying, 
  result, 
  onClose 
}: { 
  isPlaying: boolean
  result: KnowledgeCard | null
  onClose: () => void
}) {
  if (!isPlaying || !result) return null
  
  const config = rarityConfig[result.rarity]
  
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/90 backdrop-blur-md">
      <div className="text-center animate-slide-up">
        {/* 光效背景 */}
        <div className={`absolute inset-0 flex items-center justify-center pointer-events-none`}>
          <div className={`w-96 h-96 rounded-full bg-gradient-to-r ${config.color} opacity-20 blur-3xl animate-pulse`} />
        </div>
        
        {/* 卡牌展示 */}
        <div className="relative mb-8">
          <div className={`w-64 aspect-[3/4] mx-auto rounded-2xl border-2 ${config.borderColor} bg-card overflow-hidden shadow-2xl ${config.bgGlow} animate-bounce-subtle`}>
            <div className={`h-1 bg-gradient-to-r ${config.color}`} />
            <div className="p-6 h-full flex flex-col">
              <div className="flex items-center justify-between mb-4">
                <span className={`text-sm font-bold px-3 py-1 rounded-full bg-gradient-to-r ${config.color} text-white`}>
                  {config.label}
                </span>
                <div className="flex gap-1">
                  {Array.from({ length: config.stars }).map((_, i) => (
                    <Star key={i} className={`w-4 h-4 fill-current ${config.textColor}`} />
                  ))}
                </div>
              </div>
              
              <div className="flex-1 flex items-center justify-center">
                <div className={`w-24 h-24 rounded-xl bg-gradient-to-br ${config.color} flex items-center justify-center text-5xl shadow-lg animate-pulse-glow`}>
                  {result.icon}
                </div>
              </div>
              
              <div className="mt-auto text-center">
                <h3 className="text-2xl font-bold text-foreground mb-2">{result.title}</h3>
                <p className="text-sm text-muted-foreground">{result.category}</p>
              </div>
            </div>
          </div>
        </div>
        
        {/* 获得提示 */}
        <div className="mb-6">
          <h2 className={`text-3xl font-bold bg-gradient-to-r ${config.color} bg-clip-text text-transparent mb-2`}>
            恭喜获得!
          </h2>
          <p className="text-muted-foreground">你获得了一张{config.label}知识卡牌</p>
        </div>
        
        {/* 关闭按钮 */}
        <button
          onClick={onClose}
          className="px-8 py-3 bg-gradient-to-r from-primary to-secondary text-white rounded-xl font-bold hover:opacity-90 transition-opacity"
        >
          确认
        </button>
      </div>
    </div>
  )
}

export default function KnowledgeListPage() {
  const [cards] = useState<KnowledgeCard[]>(mockCards)
  const [flippedCards, setFlippedCards] = useState<Set<number>>(new Set())
  const [filterRarity, setFilterRarity] = useState<Rarity | ''>('')
  const [isGachaPlaying, setIsGachaPlaying] = useState(false)
  const [gachaResult, setGachaResult] = useState<KnowledgeCard | null>(null)

  const collectedCount = cards.filter(c => c.collected).length
  const totalCount = cards.length
  
  const filteredCards = cards.filter(c => !filterRarity || c.rarity === filterRarity)

  const handleFlip = useCallback((id: number) => {
    setFlippedCards(prev => {
      const newSet = new Set(prev)
      if (newSet.has(id)) {
        newSet.delete(id)
      } else {
        newSet.add(id)
      }
      return newSet
    })
  }, [])

  const handleGacha = () => {
    // 模拟抽卡，随机选择一张未收集的卡
    const uncollectedCards = cards.filter(c => !c.collected)
    if (uncollectedCards.length === 0) {
      alert('你已收集所有卡牌!')
      return
    }
    const randomCard = uncollectedCards[Math.floor(Math.random() * uncollectedCards.length)]
    setGachaResult(randomCard)
    setIsGachaPlaying(true)
  }

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <Header />

      <main className="flex-1">
        {/* Hero 区域 */}
        <section className="relative overflow-hidden py-16">
          {/* 背景装饰 */}
          <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-transparent to-secondary/5" />
          <div className="absolute top-10 left-10 w-72 h-72 bg-primary/10 rounded-full blur-3xl animate-float" />
          <div className="absolute bottom-10 right-10 w-96 h-96 bg-secondary/10 rounded-full blur-3xl animate-float" style={{ animationDelay: '2s' }} />
          
          <div className="container mx-auto px-4 relative">
            <div className="text-center animate-slide-up">
              <div className="inline-flex items-center gap-2 px-4 py-2 bg-primary/10 rounded-full text-primary text-sm font-medium mb-6">
                <Sparkles className="w-4 h-4" />
                知识卡牌收集
              </div>
              <h1 className="text-5xl font-bold mb-4">
                <span className="bg-gradient-to-r from-primary via-purple-500 to-secondary bg-clip-text text-transparent animate-gradient bg-[length:200%_auto]">
                  知识图鉴
                </span>
              </h1>
              <p className="text-xl text-muted-foreground mb-8">收集知识卡牌，解锁学习成就</p>
              
              {/* 收集进度 */}
              <div className="max-w-md mx-auto mb-8">
                <div className="flex items-center justify-between text-sm mb-2">
                  <span className="text-muted-foreground">收集进度</span>
                  <span className="text-primary font-bold">{collectedCount} / {totalCount}</span>
                </div>
                <div className="h-3 bg-muted rounded-full overflow-hidden">
                  <div 
                    className="h-full bg-gradient-to-r from-primary to-secondary transition-all duration-500"
                    style={{ width: `${(collectedCount / totalCount) * 100}%` }}
                  />
                </div>
              </div>
              
              {/* 抽卡按钮 */}
              <button
                onClick={handleGacha}
                className="group relative inline-flex items-center gap-3 px-8 py-4 bg-gradient-to-r from-amber-500 via-orange-500 to-red-500 text-white rounded-2xl font-bold text-lg shadow-lg shadow-orange-500/30 hover:shadow-orange-500/50 hover:scale-105 transition-all duration-300"
              >
                <Zap className="w-6 h-6 group-hover:animate-bounce" />
                抽取知识卡牌
                <Crown className="w-5 h-5" />
                
                {/* 按钮光效 */}
                <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-white/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
              </button>
            </div>
          </div>
        </section>

        {/* 稀有度筛选 */}
        <section className="bg-card/50 border-y border-border py-6">
          <div className="container mx-auto px-4">
            <div className="flex flex-wrap items-center justify-center gap-3">
              <button
                onClick={() => setFilterRarity('')}
                className={`px-4 py-2 rounded-lg font-medium transition-all ${
                  filterRarity === ''
                    ? 'bg-primary text-primary-foreground shadow-lg shadow-primary/30'
                    : 'bg-muted text-muted-foreground hover:bg-muted/80'
                }`}
              >
                全部
              </button>
              {(Object.keys(rarityConfig) as Rarity[]).map(rarity => (
                <button
                  key={rarity}
                  onClick={() => setFilterRarity(rarity)}
                  className={`px-4 py-2 rounded-lg font-medium transition-all flex items-center gap-2 ${
                    filterRarity === rarity
                      ? `bg-gradient-to-r ${rarityConfig[rarity].color} text-white shadow-lg`
                      : 'bg-muted text-muted-foreground hover:bg-muted/80'
                  }`}
                >
                  <Star className={`w-4 h-4 ${filterRarity === rarity ? 'fill-current' : ''}`} />
                  {rarityConfig[rarity].label}
                </button>
              ))}
              
              {/* 重置翻转 */}
              <button
                onClick={() => setFlippedCards(new Set())}
                className="px-4 py-2 rounded-lg font-medium bg-muted text-muted-foreground hover:bg-muted/80 transition-all flex items-center gap-2"
              >
                <RotateCcw className="w-4 h-4" />
                重置
              </button>
            </div>
          </div>
        </section>

        {/* 卡牌网格 */}
        <section className="py-12">
          <div className="container mx-auto px-4">
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 md:gap-6">
              {filteredCards.map((card, index) => (
                <div
                  key={card.id}
                  className="animate-slide-up"
                  style={{ animationDelay: `${index * 0.05}s` }}
                >
                  <KnowledgeCardComponent
                    card={card}
                    isFlipped={flippedCards.has(card.id)}
                    onFlip={() => handleFlip(card.id)}
                  />
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* 创建知识点 CTA */}
        <section className="py-12 bg-card/50 border-t border-border">
          <div className="container mx-auto px-4 text-center">
            <h2 className="text-3xl font-bold text-foreground mb-4">想要贡献新卡牌?</h2>
            <p className="text-muted-foreground mb-8 text-lg">创建新的知识点，丰富卡牌图鉴</p>
            <Link
              href="/knowledge/create"
              className="inline-flex items-center gap-2 px-8 py-3 bg-gradient-to-r from-primary to-secondary text-white font-bold rounded-xl hover:shadow-lg hover:shadow-primary/30 transition-all"
            >
              <Sparkles className="w-5 h-5" />
              创建知识卡牌
            </Link>
          </div>
        </section>
      </main>

      <Footer />
      
      {/* 抽卡动画 */}
      <GachaAnimation
        isPlaying={isGachaPlaying}
        result={gachaResult}
        onClose={() => {
          setIsGachaPlaying(false)
          setGachaResult(null)
        }}
      />
      
      {/* CSS for 3D transforms */}
      <style jsx global>{`
        .perspective-1000 {
          perspective: 1000px;
        }
        .transform-style-3d {
          transform-style: preserve-3d;
        }
        .backface-hidden {
          backface-visibility: hidden;
        }
        .rotate-y-180 {
          transform: rotateY(180deg);
        }
      `}</style>
    </div>
  )
}
