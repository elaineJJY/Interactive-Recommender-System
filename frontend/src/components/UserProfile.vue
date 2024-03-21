<template>
    <div class="page-container">
        <a-row align="top" justify="space-between">
            <a-col :span="6">
                <div class="model-container">
                    <h2>Models</h2>
                    <a-typography-text strong style="color: gray;">
                        <blockquote>Description: XXXXXX</blockquote>
                    </a-typography-text>

                    <!-- each model -->
                    <div class="model-slider-container" @mouseenter="isHovering[0] = true"
                        @mouseleave="isHovering[0] = false" :class="{ 'hover-border-topic': isHovering[0] }"
                        :tooltip-style="{ color: 'red', borderColor: 'red' }">
                        <h4>
                            Topic-Group:
                        </h4>
                        <el-slider v-model="value" size="large" color="red" />

                    </div>

                    <el-slider v-model="value" size="large" />
                    <el-slider v-model="value" size="large" />
                </div>
            </a-col>

            <a-col :span="18" @mouseenter="isHovering[0] = true" @mouseleave="isHovering[0] = false">
                <div class="chart-container" :class="{ 'hover-border-topic': isHovering[0] }">
                    <h2>Topic Preferences</h2>
                    <div style="width: 100%; height: 400px" ref="chartRef" class="chart"></div>
                    <div class="sliders-container">
                        <a-row>
                            <a-col :span="12" v-for="(item, index) in userData.topic_preferences" :key="item.id">
                                <a-row align="middle">
                                    <a-col :span="14">
                                        <a-tag :style="{ borderColor: chartColors[index] }"
                                            @mouseover="highlightChartSection(index)"
                                            @mouseleave="downplayChartSection(index)">
                                            {{ item.description }}
                                        </a-tag>
                                    </a-col>
                                    <a-col :span="8">
                                        <a-slider v-model:value="item.score" :min="0" :max="1" :step="0.01"
                                            @change="() => updateChartData(index)"
                                            @afterChange="() => downplayChartSection(index)"
                                            :tipFormatter="value => `${(value * 100).toFixed(0)}%`"
                                            :style="{ color: chartColors[index] }" />
                                    </a-col>
                                    <a-col :span="1">
                                        {{ (item.score * 100).toFixed(0) }}%
                                    </a-col>
                                </a-row>
                            </a-col>
                        </a-row>
                    </div>
                </div>
            </a-col>
        </a-row>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import * as echarts from 'echarts';
import apiClient from '@/config/apiClient';
const chartRef = ref(null);
const chartColors = ref([]);
const userData = reactive({ topic_preferences: [] });
const backup = reactive({ topic_preferences: [] });
let isHovering = ref([]); 

let chartInstance = null;

const updateChartData = (id) => {
    const updatedData = userData.topic_preferences.map((pref, index) => ({
        name: pref.description,
        value: pref.score * 100,
        selected: index == id,
    }));



    const updatedOption = {
        series: [{
            data: updatedData,
        }],
    };

    if (chartInstance) {
        chartInstance.setOption(updatedOption);
        const temp = updatedOption.series[0];
        for (let i = 0; i < temp.data.length; i++) {
            if (temp.data[i].selected) {
                highlightChartSection(i,false);
            }
        }
    }

    var sum = 0;
    for (var i = 0; i < userData.topic_preferences.length; i++) {
        sum += userData.topic_preferences[i].score;
    }
    for (var j = 0; j < userData.topic_preferences.length; j++) {
        userData.topic_preferences[j].score = userData.topic_preferences[j].score / sum;
    }


};

const highlightChartSection = (index, showTip = true) => {
    if (chartInstance) {
        chartInstance.dispatchAction({
            type: 'highlight',
            seriesIndex: 0,
            dataIndex: index
        });
        if (showTip) {
            chartInstance.dispatchAction({
                type: 'showTip',
                seriesIndex: 0,
                dataIndex: index
            });
        }
    }
};


const downplayChartSection = (index) => {
    if (chartInstance) {
        chartInstance.dispatchAction({
            type: 'downplay',
            seriesIndex: 0,
            dataIndex: index
        });
        // chartInstance.dispatchAction({
        //     type: 'showTip',
        //     seriesIndex: 0,
        //     dataIndex: index
        // });
    }
};

onMounted(async () => {
    const fetchedUserData = await apiClient.getUser();
    userData.topic_preferences = fetchedUserData.topic_preferences || [];
    backup.topic_preferences = userData.topic_preferences;
    chartInstance = echarts.init(chartRef.value);
    const data = userData.topic_preferences.map(pref => ({
        name: pref.description,
        value: pref.score * 100,
    }));

    const option = {
        tooltip: {
            trigger: 'item',
            formatter: function (params) {
                const topic = userData.topic_preferences.find(pref => pref.description === params.name);
                if (topic) {
                    const imgPath = require(`@/assets/topics/topic_${topic.id}_wordcloud.png`);
                    const percentage = params.percent.toFixed(2);
                    return `<b>${topic.description}: ${percentage}%</b><br/><img src="${imgPath}" alt="${topic.description}" style="width: 200px; height: auto;"/>`;
                }
                return `${params.name}: ${params.value}`;
            }
        },
        series: [
            {
                type: 'pie',
                radius: ['20%', '50%'],
                data: data,
                itemStyle: {
                    borderRadius: 0,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: {
                    show: true,
                    position: 'outside',
                    formatter: params => {
                        const percentage = params.percent.toFixed(2);
                        return `{a|${params.name}: ${percentage}%}`;
                    },
                    rich: {
                        a: {
                            backgroundColor: '#f0f0f0',
                            borderColor: 'auto',
                            borderWidth: 1,
                            borderRadius: 4,
                            padding: [3, 4],
                            color: '#000',
                            fontSize: 14,
                            lineHeight: 22,
                            align: 'center',
                        }
                    }
                },
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    },
                    label: {
                        show: true,
                        backgroundColor: 'auto',
                        padding: [2, 3],
                        borderRadius: 4,
                    }
                }
            }
        ]
    };

    chartInstance.setOption(option);
    chartInstance = echarts.init(chartRef.value);

    chartColors.value = chartInstance.getOption().color;

});
</script>

<style scoped>
.page-container {
    margin: -20px;
    background-color: #f0f2f5;
    min-height: 92vh;    
    box-sizing: border-box;
 
}

.chart {
    margin-top: -80px;
}

.model-container {
    background-color: #fff;
    border-radius: 8px;
    border: 1px solid #f0f0f0;
    margin-top: 10px;
    margin-bottom: 10px;
    margin-left: 10px;  
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.model-slider-container {
    background-color: #fff;
    border-radius: 8px;
    border: 3px dashed #215f9900 !important;
    margin: 5px;
    padding: 15px;
}


.chart-container {
    background-color: #fff;
    border-radius: 8px;
    border: 1px solid #f0f0f0;
    margin: 10px;
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.hover-border-topic {
    border: 3px dashed #215f99 !important;
}
</style>
