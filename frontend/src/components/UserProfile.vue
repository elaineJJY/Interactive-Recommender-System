<template>
    <div class="chart-container">
        <div style="width: 100%; height: 400px" ref="chartRef" class="chart"></div>
        <div class="sliders-container">
            <a-row>
                <a-col :span="12" v-for="(item, index) in userData.topic_preferences" :key="item.id">
                    <a-row align="middle">
                        <a-col :span="10">
                            <a-tag>{{ item.description }}</a-tag>
                        </a-col>
                        <a-col :span="8">
                            <a-slider v-model:value="item.score" :min="0" :max="1" :step="0.01"
                                @change="() => updateChartData(index)" @afterChange="() => downplayChartSection(index)"
                                :tipFormatter="value => `${(value * 100).toFixed(0)}%`" />
                        </a-col>
                        <a-col :span="4">
                            {{ (item.score * 100).toFixed(0) }}%
                        </a-col>
                    </a-row>

                </a-col>
            </a-row>

        </div>



    </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import * as echarts from 'echarts';
import apiClient from '@/config/apiClient';
const chartRef = ref(null);
const userData = reactive({ topic_preferences: [] });
const backup = reactive({ topic_preferences: [] });

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
                chartInstance.dispatchAction({
                    type: 'highlight',
                    seriesIndex: 0,
                    dataIndex: i
                });
                chartInstance.dispatchAction({
                    type: 'showTip',
                    seriesIndex: 0,
                    dataIndex: i
                });
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


const downplayChartSection = (index) => {
    if (chartInstance) {
        chartInstance.dispatchAction({
            type: 'downplay',
            seriesIndex: 0,
            dataIndex: index
        });
        chartInstance.dispatchAction({
            type: 'showTip',
            seriesIndex: 0,
            dataIndex: index
        });
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
                            borderColor: '#d9d9d9',
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
                        padding: [3, 4],
                        borderRadius: 4,
                    }
                }
            }
        ]
    };

    chartInstance.setOption(option);
});
</script>

<style scoped>
.sliders-container {
    margin-left: 10px;
}
</style>
