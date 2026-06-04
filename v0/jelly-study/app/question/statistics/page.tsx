'use client'

import { useState } from 'react'
import Header from '@/components/jelly-study/header'
import Footer from '@/components/jelly-study/footer'
import { BarChart, Bar, LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'

const statisticsData = {
  summary: {
    totalQuestions: 256,
    totalAnswers: 523,
    totalUsers: 145,
    totalKnowledgePoints: 38,
  },
  dailyQuestions: [
    { date: '6.1', count: 12 },
    { date: '6.2', count: 18 },
    { date: '6.3', count: 24 },
    { date: '6.4', count: 15 },
    { date: '6.5', count: 22 },
  ],
  categoryDistribution: [
    { name: 'Frontend', value: 98 },
    { name: 'Backend', value: 72 },
    { name: 'Language', value: 45 },
    { name: 'Database', value: 28 },
    { name: 'DevOps', value: 13 },
  ],
  difficultyDistribution: [
    { name: '简单', value: 85 },
    { name: '中等', value: 128 },
    { name: '困难', value: 43 },
  ],
  topQuestions: [
    { title: 'React Hooks 最佳实践', views: 342, answers: 8 },
    { title: 'TypeScript 高级类型', views: 287, answers: 5 },
    { title: 'Next.js 性能优化', views: 256, answers: 6 },
    { title: 'Node.js 事件循环', views: 218, answers: 4 },
    { title: 'SQL 查询优化', views: 195, answers: 3 },
  ],
}

const COLORS = ['#667eea', '#764ba2', '#f093fb', '#4facfe', '#43e97b']

export default function StatisticsPage() {
  const [timeRange] = useState('week')

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <Header />

      <main className="flex-1">
        {/* Hero 区域 */}
        <section className="bg-gradient-primary py-12">
          <div className="container mx-auto px-4">
            <div className="animate-slide-up">
              <h1 className="text-4xl font-bold text-white mb-4">平台统计信息</h1>
              <p className="text-lg text-white/80">了解平台的发展趋势和热点内容</p>
            </div>
          </div>
        </section>

        {/* 统计概览 */}
        <section className="py-12 bg-card border-b border-border">
          <div className="container mx-auto px-4">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {[
                { icon: '📝', label: '总问题数', value: statisticsData.summary.totalQuestions },
                { icon: '💬', label: '总回答数', value: statisticsData.summary.totalAnswers },
                { icon: '👥', label: '活跃用户', value: statisticsData.summary.totalUsers },
                { icon: '📚', label: '知识点数', value: statisticsData.summary.totalKnowledgePoints },
              ].map((stat, idx) => (
                <div
                  key={idx}
                  className="bg-background rounded-xl p-6 border border-border hover:border-primary transition-all animate-slide-up hover:shadow-lg"
                  style={{ animationDelay: `${idx * 0.1}s` }}
                >
                  <div className="text-4xl mb-3">{stat.icon}</div>
                  <p className="text-muted-foreground mb-2">{stat.label}</p>
                  <p className="text-3xl font-bold text-primary">{stat.value}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* 图表区域 */}
        <section className="py-12">
          <div className="container mx-auto px-4">
            {/* 问题趋势 */}
            <div className="bg-card rounded-xl p-8 border border-border mb-8 shadow-lg animate-slide-up">
              <h2 className="text-2xl font-bold text-foreground mb-6">📈 问题提交趋势</h2>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={statisticsData.dailyQuestions}>
                  <CartesianGrid stroke="var(--border)" />
                  <XAxis stroke="var(--muted-foreground)" dataKey="date" />
                  <YAxis stroke="var(--muted-foreground)" />
                  <Tooltip 
                    contentStyle={{ backgroundColor: 'var(--card)', border: '1px solid var(--border)' }}
                    labelStyle={{ color: 'var(--foreground)' }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="count" 
                    stroke="var(--primary)" 
                    strokeWidth={2}
                    dot={{ fill: 'var(--primary)', r: 4 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* 分类分布 */}
              <div className="bg-card rounded-xl p-8 border border-border shadow-lg animate-slide-up">
                <h3 className="text-xl font-bold text-foreground mb-6">🏷️ 分类分布</h3>
                <ResponsiveContainer width="100%" height={250}>
                  <PieChart>
                    <Pie
                      data={statisticsData.categoryDistribution}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={(entry) => entry.name}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {statisticsData.categoryDistribution.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip 
                      contentStyle={{ backgroundColor: 'var(--card)', border: '1px solid var(--border)' }}
                      labelStyle={{ color: 'var(--foreground)' }}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              {/* 难度分布 */}
              <div className="bg-card rounded-xl p-8 border border-border shadow-lg animate-slide-up" style={{ animationDelay: '0.1s' }}>
                <h3 className="text-xl font-bold text-foreground mb-6">⭐ 难度分布</h3>
                <ResponsiveContainer width="100%" height={250}>
                  <BarChart data={statisticsData.difficultyDistribution}>
                    <CartesianGrid stroke="var(--border)" />
                    <XAxis stroke="var(--muted-foreground)" dataKey="name" />
                    <YAxis stroke="var(--muted-foreground)" />
                    <Tooltip 
                      contentStyle={{ backgroundColor: 'var(--card)', border: '1px solid var(--border)' }}
                      labelStyle={{ color: 'var(--foreground)' }}
                    />
                    <Bar dataKey="value" fill="var(--primary)" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* 热门问题 */}
            <div className="bg-card rounded-xl p-8 border border-border shadow-lg mt-8 animate-slide-up" style={{ animationDelay: '0.2s' }}>
              <h3 className="text-2xl font-bold text-foreground mb-6">🔥 热门问题</h3>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-border">
                      <th className="text-left py-3 px-4 font-bold text-foreground">问题</th>
                      <th className="text-right py-3 px-4 font-bold text-foreground">浏览量</th>
                      <th className="text-right py-3 px-4 font-bold text-foreground">回答数</th>
                    </tr>
                  </thead>
                  <tbody>
                    {statisticsData.topQuestions.map((q, idx) => (
                      <tr 
                        key={idx} 
                        className="border-b border-border hover:bg-background transition-colors animate-slide-up"
                        style={{ animationDelay: `${0.3 + idx * 0.05}s` }}
                      >
                        <td className="py-4 px-4 text-foreground font-medium">{q.title}</td>
                        <td className="py-4 px-4 text-right text-muted-foreground">👁️ {q.views}</td>
                        <td className="py-4 px-4 text-right text-muted-foreground">💬 {q.answers}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  )
}
