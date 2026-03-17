
<template>
  <div class="page-container" style="width: 100%">
    <!-- 核心指标卡片 -->
    <a-row :gutter="16" class="metrics-row">
      <!-- DAU -->
      <a-col :span="8">
        <a-card :bordered="false" class="metric-card dau-card">
          <div class="metric-header">
            <div class="metric-icon">
              <a-icon type="calendar" theme="filled" />
            </div>
            <div class="metric-title">（日活跃用户）</div>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ activityMetrics.dau || 0 }}</div>
            <div class="metric-desc">今日有学习足迹或提问记录的用户数</div>
          </div>
          <div class="metric-footer">
            <a-progress
              :percent="calculatePercentage(activityMetrics.dau, activityMetrics.mau)"
              strokeColor="#1890ff"
              :show-info="false"
              size="small"
            />
            <div class="progress-label">占月活用户的 {{ calculatePercentage(activityMetrics.dau, activityMetrics.mau) }}%</div>
          </div>
        </a-card>
      </a-col>

      <!-- WAU -->
      <a-col :span="8">
        <a-card :bordered="false" class="metric-card wau-card">
          <div class="metric-header">
            <div class="metric-icon">
              <a-icon type="schedule" theme="filled" />
            </div>
            <div class="metric-title">（周活跃用户）</div>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ activityMetrics.wau || 0 }}</div>
            <div class="metric-desc">过去 7 天的活跃用户数</div>
          </div>
          <div class="metric-footer">
            <a-progress
              :percent="calculatePercentage(activityMetrics.wau, activityMetrics.mau)"
              strokeColor="#52c41a"
              :show-info="false"
              size="small"
            />
            <div class="progress-label">占月活用户的 {{ calculatePercentage(activityMetrics.wau, activityMetrics.mau) }}%</div>
          </div>
        </a-card>
      </a-col>

      <!-- MAU -->
      <a-col :span="8">
        <a-card :bordered="false" class="metric-card mau-card">
          <div class="metric-header">
            <div class="metric-icon">
              <a-icon type="security-scan" theme="filled" />
            </div>
            <div class="metric-title">（月活跃用户）</div>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ activityMetrics.mau || 0 }}</div>
            <div class="metric-desc">过去 30 天的活跃用户数</div>
          </div>
          <div class="metric-footer">
            <a-divider dashed style="margin: 12px 0" />
            <div class="trend-label" style="margin-top: 27px">用于长期趋势分析</div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 人均提问次数 -->
    <a-row :gutter="16" class="metrics-row">
      <a-col :span="12">
        <a-card title="人均提问次数" :bordered="false" class="stat-card">
          <div class="stat-grid">
            <div class="stat-item">
              <div class="stat-label">日均值</div>
              <div class="stat-value daily">{{ activityMetrics.avgQuestionsPerUser.dailyAvg || 0 }}</div>
              <div class="stat-unit">次/人</div>
              <div class="stat-detail">今日提问数 / 总用户数</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">周均值</div>
              <div class="stat-value weekly">{{ activityMetrics.avgQuestionsPerUser.weeklyAvg || 0 }}</div>
              <div class="stat-unit">次/人</div>
              <div class="stat-detail">本周提问数 / 总用户数</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">月均值</div>
              <div class="stat-value monthly">{{ activityMetrics.avgQuestionsPerUser.monthlyAvg || 0 }}</div>
              <div class="stat-unit">次/人</div>
              <div class="stat-detail">本月提问数 / 总用户数</div>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 单次会话时长 -->
      <a-col :span="12">
        <a-card title="单次会话时长" :bordered="false" class="stat-card">
          <div class="stat-grid">
            <div class="stat-item">
              <div class="stat-label">日均时长</div>
              <div class="stat-value daily">{{ activityMetrics.avgSessionDuration.dailyAvgMinutes || 0 }}</div>
              <div class="stat-unit">分钟</div>
              <div class="stat-detail">基于学习足迹计算</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">周均时长</div>
              <div class="stat-value weekly">{{ activityMetrics.avgSessionDuration.weeklyAvgMinutes || 0 }}</div>
              <div class="stat-unit">分钟</div>
              <div class="stat-detail">每次会话平均时长</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">月均时长</div>
              <div class="stat-value monthly">{{ activityMetrics.avgSessionDuration.monthlyAvgMinutes || 0 }}</div>
              <div class="stat-unit">分钟</div>
              <div class="stat-detail">假设每次行为代表一次会话</div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 活跃度趋势图 -->
<!--    <a-row :gutter="16" class="chart-row">-->
<!--      <a-col :span="24">-->
<!--        <a-card title="活跃度趋势分析" :bordered="false" class="trend-card">-->
<!--          <div ref="activityTrendChart" class="chart-container"></div>-->
<!--          <a-empty-->
<!--            v-if="!activityMetrics.activityTrend"-->
<!--            description="暂无活跃度趋势数据"-->
<!--            :image="simpleImage"-->
<!--            class="empty-chart"-->
<!--          />-->
<!--        </a-card>-->
<!--      </a-col>-->
<!--    </a-row>-->
  </div>
</template>

<script>import { Empty } from 'ant-design-vue'

export default {
  name: 'ActivityMetrics',
  data () {
    return {
      activityMetrics: {
        dau: 0,
        wau: 0,
        mau: 0,
        avgQuestionsPerUser: {
          dailyAvg: 0,
          weeklyAvg: 0,
          monthlyAvg: 0
        },
        avgSessionDuration: {
          dailyAvgMinutes: 0,
          weeklyAvgMinutes: 0,
          monthlyAvgMinutes: 0
        },
        activityTrend: null
      },
      simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
      chart: null
    }
  },
  mounted () {
    this.queryActivityMetrics()
  },
  methods: {
    queryActivityMetrics () {
      this.$get('/business/web/activityMetrics').then((r) => {
        if (r.data.code === 0 && r.data.data) {
          this.activityMetrics = r.data.data
          this.$nextTick(() => {
            if (this.activityMetrics.activityTrend) {
              this.renderActivityTrendChart()
            }
          })
        }
      }).catch((r) => {
        console.error(r)
        this.$message.error('获取活跃度数据失败')
      })
    },
    calculatePercentage (part, total) {
      if (!total || total === 0) return 0
      return Math.round((part / total) * 100)
    },
    renderActivityTrendChart () {
      const trendData = this.activityMetrics.activityTrend || []
      if (trendData.length === 0) return

      const dates = trendData.map(item => item.date.substring(5))
      const dauData = trendData.map(item => item.dau)
      const wauData = trendData.map(item => item.wau)
      const mauData = trendData.map(item => item.mau)

      const options = {
        series: [
          {
            name: 'DAU',
            data: dauData,
            color: '#1890ff'
          },
          {
            name: 'WAU',
            data: wauData,
            color: '#52c41a'
          },
          {
            name: 'MAU',
            data: mauData,
            color: '#722ed1'
          }
        ],
        chart: {
          type: 'line',
          height: 350,
          toolbar: {
            show: false
          },
          animations: {
            enabled: true,
            easing: 'easeinout',
            speed: 800
          }
        },
        stroke: {
          curve: 'smooth',
          width: [3, 3, 3]
        },
        fill: {
          type: 'gradient',
          gradient: {
            shadeIntensity: 1,
            opacityFrom: 0.7,
            opacityTo: 0.3,
            stops: [0, 100]
          }
        },
        markers: {
          size: 6,
          strokeWidth: 2,
          hoverSize: 8
        },
        dataLabels: {
          enabled: false
        },
        xaxis: {
          categories: dates,
          labels: {
            style: {
              fontSize: '12px',
              colors: ['#666']
            }
          },
          axisBorder: {
            show: false
          },
          axisTicks: {
            show: false
          }
        },
        yaxis: {
          title: {
            text: '活跃用户数',
            style: {
              fontSize: '13px',
              fontWeight: 500,
              color: '#666'
            }
          },
          labels: {
            style: {
              fontSize: '12px',
              colors: ['#666']
            }
          }
        },
        tooltip: {
          shared: true,
          intersect: false,
          y: {
            formatter: function (val) {
              return val + ' 人'
            }
          }
        },
        legend: {
          position: 'top',
          horizontalAlign: 'right',
          fontSize: '13px',
          markers: {
            size: 8,
            radius: 4
          }
        },
        grid: {
          borderColor: '#e8e8e8',
          strokeDashArray: 4,
          xaxis: {
            lines: {
              show: true
            }
          },
          yaxis: {
            lines: {
              show: true
            }
          }
        },
        colors: ['#1890ff', '#52c41a', '#722ed1']
      }

      if (this.chart) {
        this.chart.destroy()
      }

      this.chart = new ApexCharts(this.$refs.activityTrendChart, options)
      this.chart.render()
    }
  },
  beforeDestroy () {
    if (this.chart) {
      this.chart.destroy()
    }
  }
}
</script>

<style scoped lang="less">.page-container {
  padding: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f3f3f3 100%);
}

.metrics-row {
  margin-bottom: 24px;

  .metric-card {
    border-radius: 3px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }

    &.dau-card {
      background: linear-gradient(135deg, #ffffff 0%, #f3f3f3 100%);
    }

    &.wau-card {
      background: linear-gradient(135deg, #ffffff 0%, #f3f3f3 100%);
    }

    &.mau-card {
      background: linear-gradient(135deg, #ffffff 0%, #f3f3f3 100%);
    }

    .metric-header {
      display: flex;
      align-items: center;
      margin-bottom: 16px;

      .metric-icon {
        width: 48px;
        height: 48px;
        border-radius: 3px;
        background: rgba(255, 255, 255, 0.9);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        i {
          font-size: 24px;
          color: #1890ff;
        }
      }

      .metric-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
    }

    .metric-content {
      margin-bottom: 16px;

      .metric-value {
        font-size: 42px;
        font-weight: 700;
        color: #1890ff;
        line-height: 1;
        margin-bottom: 8px;
      }

      .metric-desc {
        font-size: 13px;
        color: #666;
        line-height: 1.5;
      }
    }

    .metric-footer {
      .progress-label {
        font-size: 12px;
        color: #999;
        margin-top: 8px;
        text-align: right;
      }

      .trend-label {
        font-size: 13px;
        color: #666;
        text-align: center;
        font-style: italic;
      }
    }
  }
}

.stat-card {
  border-radius: 3px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);

  /deep/ .ant-card-head {
    background: transparent;
    border-bottom: 2px solid #f0f0f0;
    padding: 16px 20px;

    /deep/ .ant-card-head-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }

  .stat-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    padding: 16px;

    .stat-item {
      text-align: center;
      padding: 20px;
      background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
      border-radius: 3px;
      border: 2px solid #e8e8e8;
      transition: all 0.3s;

      &:hover {
        border-color: #1890ff;
        background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
      }

      .stat-label {
        font-size: 13px;
        color: #666;
        margin-bottom: 12px;
        font-weight: 500;
      }

      .stat-value {
        font-size: 32px;
        font-weight: 700;
        margin-bottom: 8px;
        line-height: 1;

        &.daily {
          color: #1890ff;
        }

        &.weekly {
          color: #52c41a;
        }

        &.monthly {
          color: #722ed1;
        }
      }

      .stat-unit {
        font-size: 12px;
        color: #999;
        margin-bottom: 8px;
      }

      .stat-detail {
        font-size: 11px;
        color: #bbb;
        line-height: 1.4;
      }
    }
  }
}

.chart-row {
  .trend-card {
    border-radius: 3px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    background: rgba(255, 255, 255, 0.95);

    /deep/ .ant-card-head {
      background: transparent;
      border-bottom: 2px solid #f0f0f0;
      padding: 16px 20px;

      /deep/ .ant-card-head-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
    }

    .chart-container {
      height: 350px;
      width: 100%;
      padding: 16px 0;
    }

    .empty-chart {
      padding: 60px 0;
    }
  }
}
</style>
