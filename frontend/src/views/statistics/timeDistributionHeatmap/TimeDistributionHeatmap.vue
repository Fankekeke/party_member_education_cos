
<template>
  <div class="page-container" style="width: 100%">
    <a-row :gutter="16" class="content-row">
      <!-- 左侧热力图 -->
      <a-col :span="24" style="margin-bottom: 15px">
        <a-card title="用户活跃时间分布热力图" :bordered="false" class="heatmap-card">
          <div class="heatmap-wrapper">
            <!-- 小时标签 -->
<!--            <div class="hour-labels">-->
<!--              <div class="label-placeholder"></div>-->
<!--              <div v-for="hour in 24" :key="hour" class="hour-label">-->
<!--                {{ (hour - 1).toString().padStart(2, '0') }}:00-->
<!--              </div>-->
<!--            </div>-->

            <!-- 热力图主体 -->
            <div class="heatmap-grid">
              <!-- 星期标签 -->
              <div class="day-labels">
                <div v-for="(day, index) in heatmapData.weekData" :key="index" class="day-label">
                  {{ day.dayName }}
                </div>
              </div>

              <!-- 热力单元格 -->
              <div class="heatmap-cells">
                <div
                  v-for="(day, dayIndex) in heatmapData.weekData"
                  :key="dayIndex"
                  class="cell-row"
                >
                  <div
                    v-for="(count, hourIndex) in day.hourlyCounts"
                    :key="hourIndex"
                    class="heatmap-cell"
                    :style="{ backgroundColor: getHeatColor(count) }"
                    @mouseenter="showTooltip($event, day, hourIndex, count)"
                    @mouseleave="hideTooltip"
                  >
                    <span v-if="count > 0" class="cell-value">{{ count }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 图例 -->
          <div class="heatmap-legend">
            <div class="legend-item">
              <span class="legend-label">低</span>
              <div class="legend-gradient">
                <div class="gradient-bar">
                  <div class="gradient-color" style="background: #f0f9ff;"></div>
                  <div class="gradient-color" style="background: #bae7ff;"></div>
                  <div class="gradient-color" style="background: #40a9ff;"></div>
                  <div class="gradient-color" style="background: #096dd9;"></div>
                  <div class="gradient-color" style="background: #003a8c;"></div>
                </div>
              </div>
              <span class="legend-label">高</span>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧统计信息 -->
      <a-col :span="12">
        <!-- 最佳时间推荐 -->
        <a-card title="🎯 最佳时间推荐" :bordered="false" class="recommend-card">
          <div class="recommend-content">
            <div class="recommend-item">
              <div class="recommend-icon">
                <a-icon type="calendar" theme="filled" />
              </div>
              <div class="recommend-info">
                <div class="recommend-label">最佳日期</div>
                <div class="recommend-value">{{ heatmapData.bestTimeSuggestion.bestDay || '-' }}</div>
              </div>
            </div>

            <div class="recommend-item">
              <div class="recommend-icon">
                <a-icon type="clock-circle" theme="filled" />
              </div>
              <div class="recommend-info">
                <div class="recommend-label">最佳时段</div>
                <div class="recommend-value">{{ heatmapData.bestTimeSuggestion.specificTimeRange || '-' }}</div>
              </div>
            </div>

            <div class="recommend-item">
              <div class="recommend-icon">
                <a-icon type="star" theme="filled" />
              </div>
              <div class="recommend-info">
                <div class="recommend-label">推荐等级</div>
                <div class="recommend-stars">
                  <a-icon
                    v-for="i in 5"
                    :key="i"
                    type="star"
                    :theme="i <= (heatmapData.bestTimeSuggestion.recommendationLevel || 0) ? 'filled' : 'outlined'"
                    :class="{ 'star-active': i <= (heatmapData.bestTimeSuggestion.recommendationLevel || 0) }"
                  />
                </div>
              </div>
            </div>

            <div class="recommend-item">
              <div class="recommend-icon">
                <a-icon type="bar-chart" theme="filled" />
              </div>
              <div class="recommend-info">
                <div class="recommend-label">平均提问数</div>
                <div class="recommend-value">{{ heatmapData.bestTimeSuggestion.avgQuestions || 0 }} 次</div>
              </div>
            </div>

            <a-alert
              message="建议在此时间段进行学习活动，效率更高哦！"
              type="success"
              show-icon
              class="recommend-tip"
            />
          </div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <!-- 时段分布统计 -->
        <a-card title="📊 时段分布统计" :bordered="false" class="timeslot-card">
          <div class="timeslot-list">
            <div class="timeslot-item">
              <div class="timeslot-label">
                <a-icon type="sun" theme="filled" style="color: #faad14; margin-right: 8px;" />
                早晨 (6-8 点)
              </div>
              <a-progress
                :percent="calculateTimeslotPercentage(heatmapData.timeSlotSummary.morningCount)"
                strokeColor="#faad14"
                :format="() => heatmapData.timeSlotSummary.morningCount + ' 次'"
                size="small"
              />
            </div>

            <div class="timeslot-item">
              <div class="timeslot-label">
                <a-icon type="cloud-sun" theme="filled" style="color: #52c41a; margin-right: 8px;" />
                上午 (9-11 点)
              </div>
              <a-progress
                :percent="calculateTimeslotPercentage(heatmapData.timeSlotSummary.forenoonCount)"
                strokeColor="#52c41a"
                :format="() => heatmapData.timeSlotSummary.forenoonCount + ' 次'"
                size="small"
              />
            </div>

            <div class="timeslot-item">
              <div class="timeslot-label">
                <a-icon type="sun" theme="filled" style="color: #ff7875; margin-right: 8px;" />
                中午 (12-13 点)
              </div>
              <a-progress
                :percent="calculateTimeslotPercentage(heatmapData.timeSlotSummary.noonCount)"
                strokeColor="#ff7875"
                :format="() => heatmapData.timeSlotSummary.noonCount + ' 次'"
                size="small"
              />
            </div>

            <div class="timeslot-item">
              <div class="timeslot-label">
                <a-icon type="cloudy" theme="filled" style="color: #1890ff; margin-right: 8px;" />
                下午 (14-17 点)
              </div>
              <a-progress
                :percent="calculateTimeslotPercentage(heatmapData.timeSlotSummary.afternoonCount)"
                strokeColor="#1890ff"
                :format="() => heatmapData.timeSlotSummary.afternoonCount + ' 次'"
                size="small"
              />
            </div>

            <div class="timeslot-item">
              <div class="timeslot-label">
                <a-icon type="moon" theme="filled" style="color: #722ed1; margin-right: 8px;" />
                傍晚 (18-21 点)
              </div>
              <a-progress
                :percent="calculateTimeslotPercentage(heatmapData.timeSlotSummary.eveningCount)"
                strokeColor="#722ed1"
                :format="() => heatmapData.timeSlotSummary.eveningCount + ' 次'"
                size="small"
              />
            </div>

            <div class="timeslot-item">
              <div class="timeslot-label">
                <a-icon type="moon" theme="filled" style="color: #13c2c2; margin-right: 8px;" />
                夜晚 (22-23 点)
              </div>
              <a-progress
                :percent="calculateTimeslotPercentage(heatmapData.timeSlotSummary.nightCount)"
                strokeColor="#13c2c2"
                :format="() => heatmapData.timeSlotSummary.nightCount + ' 次'"
                size="small"
              />
            </div>

            <div class="timeslot-item">
              <div class="timeslot-label">
                <a-icon type="bulb" theme="filled" style="color: #eb2f96; margin-right: 8px;" />
                深夜 (0-5 点)
              </div>
              <a-progress
                :percent="calculateTimeslotPercentage(heatmapData.timeSlotSummary.lateNightCount)"
                strokeColor="#eb2f96"
                :format="() => heatmapData.timeSlotSummary.lateNightCount + ' 次'"
                size="small"
              />
            </div>

            <a-divider dashed style="margin: 16px 0" />

            <div class="most-active">
              <div class="most-active-label">
                <a-icon type="fire" theme="filled" style="color: #ff4d4f; margin-right: 8px;" />
                最活跃时段
              </div>
              <div class="most-active-value">
                {{ heatmapData.timeSlotSummary.mostActiveTimeSlot || '-' }}
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- Tooltip -->
    <div v-if="tooltip.visible" class="heatmap-tooltip" :style="tooltip.style">
      <div class="tooltip-title">{{ tooltip.dayName }}</div>
      <div class="tooltip-time">{{ tooltip.hour }}:00 - {{ tooltip.hour + 1 }}:00</div>
      <div class="tooltip-count">提问数：<strong>{{ tooltip.count }}</strong></div>
    </div>
  </div>
</template>

<script>export default {
  name: "TimeDistributionHeatmap",
  data () {
    return {
      heatmapData: {
        weekData: [],
        bestTimeSuggestion: {},
        timeSlotSummary: {}
      },
      tooltip: {
        visible: false,
        dayName: '',
        hour: 0,
        count: 0,
        style: {
          left: '0px',
          top: '0px'
        }
      },
      maxCount: 0
    }
  },
  mounted () {
    this.queryTimeDistributionHeatmap()
  },
  methods: {
    queryTimeDistributionHeatmap () {
      this.$get('/business/web/timeDistributionHeatmap').then((r) => {
        if (r.data.code === 0 && r.data.data) {
          this.heatmapData = r.data.data
          this.calculateMaxCount()
        }
      }).catch((r) => {
        console.error(r)
        this.$message.error('获取时间分布数据失败')
      })
    },
    calculateMaxCount () {
      let max = 0
      this.heatmapData.weekData.forEach(day => {
        day.hourlyCounts.forEach(count => {
          if (count > max) max = count
        })
      })
      this.maxCount = max || 1
    },
    getHeatColor (count) {
      if (count === 0) return '#f0f0f0'

      const ratio = count / this.maxCount
      if (ratio < 0.2) return '#e6f7ff'
      if (ratio < 0.4) return '#bae7ff'
      if (ratio < 0.6) return '#40a9ff'
      if (ratio < 0.8) return '#096dd9'
      return '#003a8c'
    },
    showTooltip (event, day, hour, count) {
      this.tooltip.visible = true
      this.tooltip.dayName = day.dayName
      this.tooltip.hour = hour
      this.tooltip.count = count

      const rect = event.target.getBoundingClientRect()
      this.tooltip.style = {
        left: (rect.left + window.scrollX) + 'px',
        top: (rect.top + window.scrollY - 80) + 'px'
      }
    },
    hideTooltip () {
      this.tooltip.visible = false
    },
    calculateTimeslotPercentage (count) {
      const total = this.getTotalCount()
      if (total === 0) return 0
      return Math.round((count / total) * 100)
    },
    getTotalCount () {
      let total = 0
      this.heatmapData.weekData.forEach(day => {
        total += day.totalCount
      })
      return total
    }
  }
}
</script>

<style scoped lang="less">.page-container {
  padding: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f3f3f3 100%);
  min-height: calc(100vh - 64px);
}

.content-row {
  .heatmap-card {
    border-radius: 3px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    height: 100%;

    /deep/ .ant-card-head {
      background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
      border-bottom: 2px solid #1890ff;
      padding: 16px 20px;

      /deep/ .ant-card-head-title {
        font-size: 16px;
        font-weight: 600;
        color: #003a8c;
      }
    }

    .heatmap-wrapper {
      display: flex;
      gap: 12px;
      padding: 20px 0;
    }

    .hour-labels {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .label-placeholder {
        height: 40px;
        width: 50px;
      }

      .hour-label {
        height: 40px;
        width: 50px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 11px;
        color: #666;
        font-weight: 500;
        background: #fafafa;
        border-radius: 4px;
      }
    }

    .heatmap-grid {
      display: flex;
      flex: 1;
      gap: 4px;
    }

    .day-labels {
      display: flex;
      flex-direction: column;
      gap: 4px;
      width: 60px;

      .day-label {
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 13px;
        font-weight: 600;
        color: #333;
        background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
        border-radius: 4px;
        border: 2px solid #d9d9d9;
      }
    }

    .heatmap-cells {
      display: flex;
      flex-direction: column;
      gap: 4px;
      flex: 1;

      .cell-row {
        display: flex;
        gap: 4px;
        height: 40px;

        .heatmap-cell {
          flex: 1;
          min-width: 20px;
          height: 100%;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          transition: all 0.3s;
          border: 1px solid rgba(0, 0, 0, 0.05);

          &:hover {
            transform: scale(1.1);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
            z-index: 10;
          }

          .cell-value {
            font-size: 11px;
            font-weight: 600;
            color: white;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
          }
        }
      }
    }

    .heatmap-legend {
      margin-top: 20px;
      padding: 16px;
      background: #fafafa;
      border-radius: 3px;

      .legend-item {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;

        .legend-label {
          font-size: 12px;
          color: #666;
          font-weight: 500;
        }

        .legend-gradient {
          flex: 1;
          max-width: 300px;

          .gradient-bar {
            display: flex;
            height: 20px;
            border-radius: 4px;
            overflow: hidden;

            .gradient-color {
              flex: 1;
              height: 100%;
            }
          }
        }
      }
    }
  }

  .recommend-card,
  .timeslot-card {
    border-radius: 3px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    margin-bottom: 16px;

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
  }

  .recommend-card {
    .recommend-content {
      .recommend-item {
        display: flex;
        align-items: center;
        padding: 12px;
        margin-bottom: 12px;
        background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
        border-radius: 3px;
        border: 2px solid #e8e8e8;
        transition: all 0.3s;

        &:hover {
          border-color: #1890ff;
          background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
          transform: translateX(4px);
        }

        .recommend-icon {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;
          box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);

          i {
            font-size: 18px;
            color: white;
          }
        }

        .recommend-info {
          flex: 1;

          .recommend-label {
            font-size: 12px;
            color: #999;
            margin-bottom: 4px;
          }

          .recommend-value {
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }
        }

        .recommend-stars {
          .anticon {
            font-size: 16px;
            color: #ffd700;

            &.star-active {
              color: #faad14;
            }
          }
        }
      }

      .recommend-tip {
        margin-top: 16px;
        border-radius: 3px;
      }
    }
  }

  .timeslot-card {
    .timeslot-list {
      padding: 8px;

      .timeslot-item {
        margin-bottom: 16px;

        .timeslot-label {
          font-size: 13px;
          font-weight: 600;
          color: #333;
          margin-bottom: 8px;
          display: flex;
          align-items: center;
        }
      }

      .most-active {
        padding: 16px;
        background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
        border-radius: 3px;
        border: 2px solid #ffccc7;

        .most-active-label {
          font-size: 14px;
          font-weight: 600;
          color: #cf1322;
          margin-bottom: 8px;
          display: flex;
          align-items: center;
        }

        .most-active-value {
          font-size: 20px;
          font-weight: 700;
          color: #cf1322;
        }
      }
    }
  }
}

.heatmap-tooltip {
  position: absolute;
  z-index: 1000;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.85);
  border-radius: 3px;
  color: white;
  font-size: 13px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  pointer-events: none;
  backdrop-filter: blur(4px);

  .tooltip-title {
    font-weight: 600;
    margin-bottom: 4px;
    font-size: 14px;
  }

  .tooltip-time {
    color: #bae7ff;
    margin-bottom: 6px;
    font-size: 12px;
  }

  .tooltip-count {
    strong {
      color: #52c41a;
      font-size: 16px;
    }
  }
}
</style>
