
<template>
  <div class="page-container" style="width: 100%">
    <!-- 词云展示区 -->
    <a-row :gutter="16" class="wordcloud-row">
      <a-col :span="16">
        <a-card title="☁️ 高频词云" :bordered="false" class="wordcloud-card">
          <div class="wordcloud-wrapper">
            <div ref="wordCloudContainer" class="wordcloud-container"></div>
            <a-empty
              v-if="!wordCloudData.wordCloud || wordCloudData.wordCloud.length === 0"
              description="暂无词云数据"
              :image="simpleImage"
              class="empty-wordcloud"
            />
          </div>
        </a-card>
      </a-col>

      <!-- 分类统计 -->
      <a-col :span="8">
        <a-card title="📊 分类分析" :bordered="false" class="category-card">
          <div class="category-list">
            <div class="category-item">
              <div class="category-header theory">
                <a-icon type="book" theme="filled" />
                <span>理论知识</span>
              </div>
              <div
                v-for="(item, index) in wordCloudData.categoryAnalysis.theoryTopics"
                :key="index"
                class="topic-item"
              >
                <div class="topic-name">{{ item.topic }}</div>
                <a-progress
                  :percent="item.percentage * 2"
                  strokeColor="#722ed1"
                  :format="() => item.count + '次 (' + item.percentage + '%)'"
                  size="small"
                />
              </div>
            </div>

            <div class="category-item">
              <div class="category-header policy">
                <a-icon type="file-text" theme="filled" />
                <span>政策热点</span>
              </div>
              <div
                v-for="(item, index) in wordCloudData.categoryAnalysis.policyTopics"
                :key="index"
                class="topic-item"
              >
                <div class="topic-name">{{ item.topic }}</div>
                <a-progress
                  :percent="item.percentage * 2"
                  strokeColor="#1890ff"
                  :format="() => item.count + '次 (' + item.percentage + '%)'"
                  size="small"
                />
              </div>
            </div>

            <div class="category-item">
              <div class="category-header practice">
                <a-icon type="tool" theme="filled" />
                <span>实践应用</span>
              </div>
              <div
                v-for="(item, index) in wordCloudData.categoryAnalysis.practiceTopics"
                :key="index"
                class="topic-item"
              >
                <div class="topic-name">{{ item.topic }}</div>
                <a-progress
                  :percent="item.percentage * 2"
                  strokeColor="#52c41a"
                  :format="() => item.count + '次 (' + item.percentage + '%)'"
                  size="small"
                />
              </div>
              <a-empty
                v-if="!wordCloudData.categoryAnalysis.practiceTopics || wordCloudData.categoryAnalysis.practiceTopics.length === 0"
                description="暂无实践应用类话题"
                :image="simpleImage"                style="padding: 20px 0;"
              />
            </div>

            <div class="category-item">
              <div class="category-header other">
                <a-icon type="appstore" theme="filled" />
                <span>其他</span>
              </div>
              <div class="other-stats">
                <a-statistic
                  title="其他类话题总数"
                  :value="calculateOtherCount()"
                  :value-style="{ fontSize: '24px', fontWeight: 'bold' }"
                />
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 话题热度排行 -->
    <a-row :gutter="16" class="ranking-row">
      <a-col :span="24">
        <a-card title="🔥 话题热度排行榜" :bordered="false" class="ranking-card">
          <div class="ranking-list">
            <div
              v-for="(item, index) in wordCloudData.topicRanking"
              :key="index"
              class="ranking-item"
              :class="'rank-' + (item.rank)"
            >
              <div class="ranking-left">
                <div class="ranking-badge" :class="'badge-' + (item.rank <= 3 ? 'top' : 'normal')">
                  <span v-if="item.rank <= 3" class="medal">
                    <a-icon v-if="item.rank === 1" type="trophy" theme="filled" style="color: #ffd700;" />
                    <a-icon v-else-if="item.rank === 2" type="star" theme="filled" style="color: #c0c0c0;" />
                    <a-icon v-else-if="item.rank === 3" type="star" theme="filled" style="color: #cd7f32;" />
                  </span>
                  <span v-else class="rank-number">{{ item.rank }}</span>
                </div>
                <div class="ranking-info">
                  <div class="ranking-keyword">{{ item.keyword }}</div>
                  <div class="ranking-meta">
                    <a-tag color="blue">{{ item.mentionCount }} 次提及</a-tag>
                    <a-tag color="green">{{ item.participantCount }} 人参与</a-tag>
                    <a-tag color="orange">
                      <a-icon type="fire" theme="filled" />
                      热度 {{ item.hotScore }}
                    </a-tag>
                    <a-tag :color="item.trend === 0 ? 'blue' : 'red'">
                      <a-icon :type="item.trend === 0 ? 'minus' : 'rise'" />
                      {{ item.trend === 0 ? '平稳' : '上升' }}
                    </a-tag>
                  </div>
                </div>
              </div>
              <div class="ranking-right">
                <a-popover title="相关问题" trigger="hover">
                  <template slot="content">
                    <div class="related-questions">
                      <div
                        v-for="(question, qIndex) in item.relatedQuestions"
                        :key="qIndex"
                        class="related-question"
                        v-if="question"
                      >
                        <a-icon type="link" />
                        {{ question }}
                      </div>
                    </div>
                  </template>
                  <a-button type="link" icon="eye">
                    查看关联问题
                  </a-button>
                </a-popover>
              </div>
            </div>

            <a-empty
              v-if="!wordCloudData.topicRanking || wordCloudData.topicRanking.length === 0"
              description="暂无话题排行数据"
              :image="simpleImage"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 新兴话题 -->
    <a-row :gutter="16" class="emerging-row" v-if="wordCloudData.emergingTopics && wordCloudData.emergingTopics.length > 0">
      <a-col :span="24">
        <a-card title="🚀 新兴话题" :bordered="false" class="emerging-card">
          <div class="emerging-list">
            <a-tag
              v-for="(topic, index) in wordCloudData.emergingTopics"
              :key="index"
              color="cyan"
              class="emerging-tag"
            >
              <a-icon type="rocket" theme="filled" />
              {{ topic }}
            </a-tag>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>import { Empty } from 'ant-design-vue'

export default {
  name: "HighFrequencyWordCloud",
  data () {
    return {
      wordCloudData: {
        wordCloud: [],
        topicRanking: [],
        categoryAnalysis: {
          theoryTopics: [],
          policyTopics: [],
          practiceTopics: []
        },
        emergingTopics: []
      },
      simpleImage: Empty.PRESENTED_IMAGE_SIMPLE,
      chart: null
    }
  },
  mounted () {
    this.queryHighFrequencyWordCloud()
  },
  methods: {
    queryHighFrequencyWordCloud () {
      this.$get('/business/web/highFrequencyWordCloud').then((r) => {
        if (r.data.code === 0 && r.data.data) {
          this.wordCloudData = r.data.data
          this.$nextTick(() => {
            this.renderWordCloud()
          })
        }
      }).catch((r) => {
        console.error(r)
        this.$message.error('获取词云数据失败')
      })
    },
    renderWordCloud () {
      const words = this.wordCloudData.wordCloud || []
      if (words.length === 0) return

      const seriesData = words.map(item => ({
        x: item.word,
        y: Math.max(item.weight, 1),
        z: item.frequency
      }))

      const options = {
        series: [{
          type: 'treemap',
          data: seriesData
        }],
        chart: {
          height: 400,
          width: '100%',
          type: 'treemap',
          toolbar: {
            show: false
          },
          animations: {
            enabled: true,
            easing: 'easeinout',
            speed: 800
          }
        },
        plotOptions: {
          treemap: {
            enableShades: true,
            shadeIntensity: 0.5,
            reverseNegativeShade: false,
            distributed: true,
            borderRadius: 8,
            useFillColorAsStroke: false,
            dataLabels: {
              format: 'scale'
            }
          }
        },
        stroke: {
          show: true,
          width: 2,
          colors: ['rgba(255, 255, 255, 0.9)']
        },
        fill: {
          type: 'gradient',
          gradient: {
            shade: 'light',
            shadeIntensity: 0.3,
            opacityFrom: 0.9,
            opacityTo: 0.7,
            stops: [0, 100]
          }
        },
        dataLabels: {
          enabled: true,
          style: {
            fontSize: '13px',
            fontWeight: 600,
            fontFamily: 'Helvetica, Arial, sans-serif',
            colors: ['#fff']
          },
          formatter: function (text, op) {
            return Array.isArray(text) ? text[0] : text
          },
          textAnchor: 'middle',
          offsetX: 0,
          offsetY: 0
        },
        legend: {
          show: false
        },
        tooltip: {
          enabled: true,
          custom: function ({ series, seriesIndex, dataPointIndex, w }) {
            const wordData = words[dataPointIndex]
            if (!wordData) return ''
            return `
              <div style="padding: 12px; background: rgba(0,0,0,0.85); border-radius: 8px; color: white; min-width: 200px;">
                <div style="font-size: 15px; font-weight: 600; margin-bottom: 8px; color: #40a9ff;">${wordData.word}</div>
                <div style="font-size: 12px; margin-bottom: 4px; color: #bae7ff;">类别：<span style="color: #fff;">${wordData.category}</span></div>
                <div style="font-size: 12px; margin-bottom: 4px; color: #bae7ff;">频次：<span style="color: #52c41a; font-weight: 600;">${wordData.frequency} 次</span></div>
                <div style="font-size: 12px; color: #bae7ff;">权重：<span style="color: #faad14; font-weight: 600;">${wordData.weight}</span></div>
              </div>
            `
          }
        },
        colors: [
          '#1890ff', '#52c41a', '#faad14', '#722ed1', '#13c2c2',
          '#f5222d', '#eb2f96', '#fa8c16', '#a0d911', '#2f54eb'
        ],
        grid: {
          padding: {
            left: 10,
            right: 10,
            top: 10,
            bottom: 10
          }
        }
      }

      if (this.chart) {
        this.chart.destroy()
      }

      this.$nextTick(() => {
        if (this.$refs.wordCloudContainer) {
          this.chart = new ApexCharts(this.$refs.wordCloudContainer, options)
          this.chart.render().catch(err => {
            console.error('词云图表渲染失败:', err)
          })
        }
      })
    },
    calculateOtherCount () {
      let count = 0
      if (this.wordCloudData.wordCloud) {
        this.wordCloudData.wordCloud.forEach(item => {
          if (item.category === '其他') {
            count += item.frequency
          }
        })
      }
      return count
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
  min-height: calc(100vh - 64px);
}

.wordcloud-row,
.ranking-row,
.emerging-row {
  margin-bottom: 24px;
}

.wordcloud-card,
.category-card,
.ranking-card,
.emerging-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);

  /deep/ .ant-card-head {
    background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
    border-bottom: 2px solid #1890ff;
    padding: 16px 20px;

    /deep/ .ant-card-head-title {
      font-size: 16px;
      font-weight: 600;
      color: #003a8c;
    }
  }
}

.wordcloud-container {
  height: 400px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.category-card {
  .category-list {
    .category-item {
      margin-bottom: 20px;
      padding: 16px;
      background: #fafafa;
      border-radius: 3px;
      border: 2px solid #e8e8e8;

      &:last-child {
        margin-bottom: 0;
      }

      .category-header {
        font-size: 15px;
        font-weight: 600;
        color: #333;
        margin-bottom: 16px;
        display: flex;
        align-items: center;
        gap: 8px;
        padding-bottom: 12px;
        border-bottom: 2px dashed #d9d9d9;

        i {
          font-size: 18px;
        }

        &.theory {
          color: #722ed1;
          background: linear-gradient(135deg, #f9f0ff 0%, #efdbff 100%);
          padding: 8px 12px;
          border-radius: 3px;
          border: none;
        }

        &.policy {
          color: #1890ff;
          background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
          padding: 8px 12px;
          border-radius: 3px;
          border: none;
        }

        &.practice {
          color: #52c41a;
          background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
          padding: 8px 12px;
          border-radius: 3px;
          border: none;
        }

        &.other {
          color: #faad14;
          background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
          padding: 8px 12px;
          border-radius: 6px;
          border: none;
        }
      }

      .topic-item {
        margin-bottom: 12px;
        width: 90%;

        .topic-name {
          font-size: 13px;
          color: #333;
          margin-bottom: 8px;
          font-weight: 500;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .other-stats {
        text-align: center;
        padding: 12px;
      }
    }
  }
}

.ranking-card {
  .ranking-list {
    .ranking-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      margin-bottom: 12px;
      background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
      border-radius: 3px;
      border-left: 4px solid #1890ff;
      transition: all 0.3s;

      &:hover {
        transform: translateX(8px);
        box-shadow: 0 4px 16px rgba(24, 144, 255, 0.15);
      }

      &.rank-1 {
        border-left-color: #ffd700;
        background: linear-gradient(135deg, #fffbe6 0%, #fff1b8 100%);
      }

      &.rank-2 {
        border-left-color: #c0c0c0;
        background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
      }

      &.rank-3 {
        border-left-color: #cd7f32;
        background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
      }

      .ranking-left {
        display: flex;
        align-items: center;
        flex: 1;

        .ranking-badge {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 18px;
          font-weight: 700;
          margin-right: 16px;
          flex-shrink: 0;

          &.badge-top {
            color: white;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);

            .medal {
              i {
                font-size: 20px;
              }
            }
          }

          &.badge-normal {
            background: #f0f0f0;
            color: #666;
            border: 2px solid #d9d9d9;
          }
        }

        .ranking-info {
          flex: 1;

          .ranking-keyword {
            font-size: 15px;
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
            line-height: 1.5;
          }

          .ranking-meta {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
          }
        }
      }

      .ranking-right {
        flex-shrink: 0;
      }
    }
  }

  .related-questions {
    max-width: 400px;
    max-height: 300px;
    overflow-y: auto;

    .related-question {
      padding: 8px;
      margin-bottom: 8px;
      background: #fafafa;
      border-radius: 4px;
      font-size: 13px;
      line-height: 1.6;
      color: #666;

      i {
        margin-right: 4px;
        color: #1890ff;
      }

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.emerging-card {
  .emerging-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    padding: 16px;

    .emerging-tag {
      font-size: 14px;
      padding: 6px 16px;
      border-radius: 3px;
      border: 2px solid #13c2c2;
      background: linear-gradient(135deg, #e6fffb 0%, #b5f5ec 100%);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(19, 194, 194, 0.3);
      }

      i {
        margin-right: 4px;
      }
    }
  }
}

.wordcloud-card {
  .wordcloud-wrapper {
    position: relative;
    min-height: 400px;

    .wordcloud-container {
      height: 400px;
      width: 100%;

      svg {
        width: 100% !important;
        height: 100% !important;
      }
    }

    .empty-wordcloud {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 100%;
    }
  }
}

.category-card {
  height: 550px;
  overflow-y: auto;
  overflow-x: hidden;

  /deep/ .ant-card-body {
    height: 450px;
    overflow-y: auto;
    padding: 16px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: #f5f5f5;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d9d9d9;
      border-radius: 3px;

      &:hover {
        background: #bfbfbf;
      }
    }
  }
}
.ranking-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  margin-right: 16px;
  flex-shrink: 0;
  background: #f0f0f0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

  &.badge-top {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.7) 100%);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

    .medal {
      i {
        font-size: 20px;
        filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
      }
    }
  }

  &.badge-normal {
    background: #f5f5f5;
    color: #666;
    border: 2px solid #d9d9d9;

    .rank-number {
      font-size: 16px;
      font-weight: 600;
      color: #666;
    }
  }
}
</style>
