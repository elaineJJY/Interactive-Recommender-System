<template>
    <div class="page-container">
        <a-row align="top" justify="space-between">
            <a-col :span="6">
                <div class="model-container">
                    <h2>Models</h2>
                    <a-typography-text strong style="color: gray;">
                        <blockquote>
                            Here, you can tailor your video feed by modifying the balance among various recommendation
                            models, shaping the selection of every 10-video batch to better match your interests.
                        </blockquote>
                    </a-typography-text>

                    <!-- Models -->
                    <div v-for="(val,index) in models" :key="index" class="model-slider-container"
                        @mouseenter="isHovering[index] = true" @mouseleave="isHovering[index] = false"
                        :class="{ 'hover-border-topic': isHovering[index] }">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <div style="font-size: 16px; font-weight: bold;">Model {{ index + 1 }}</div>
                            <a-button @click="showDetails[index] = true" shape="circle" :icon="h(InfoOutlined)"
                                size="small">
                            </a-button>
                        </div>

                        <el-input-number class="input-spacing" v-model="models[index]" :min="0"
                            :max="maxModelValue(index)" :step="1" />
                        <el-dialog v-model="showDetails[index]" title="Model Details" width="30%" center>
                            <p>XXXXXX</p>
                        </el-dialog>
                    </div>


                    <div style="margin-top: 20px;">
                        <h3>Remaining Points: {{ remainingPoints }}</h3>
                    </div>

                </div>


            </a-col>

            <a-col :span="18" @mouseenter="isHovering[0] = true" @mouseleave="isHovering[0] = false">
                <div class="chart-container" :class="{ 'hover-border-topic': isHovering[0] }">
                    <h2>Topic Preferences</h2>

                    <!-- Slider -->
                    <div class="custom-slider-container">
                        <a-row justify="center" align="middle">
                            <a-col :span="5">
                                <b>Pure exloitative</b>
                            </a-col>
                            <a-col :span="10">
                                <a-slider :min="0" :max="100" defaultValue="50">
                                </a-slider>
                            </a-col>
                            <a-col :span="5">
                                <b>Pure explorative</b>
                            </a-col>
                        </a-row>
                    </div>
                    <!-- Charts -->
                    <div style="width: 100%; height: 400px" ref="chartRef" class="chart"></div>

                    <!-- Sliders -->
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
import { ref, onMounted, reactive, h, computed} from 'vue';
import * as echarts from 'echarts';
import apiClient from '@/config/apiClient';
import { InfoOutlined } from '@ant-design/icons-vue';
const chartRef = ref(null);
const chartColors = ref([]);
const isHovering = ref([]);
const showDetails = ref([]);

const userData = reactive({ topic_preferences: [] });
const backup = reactive({ topic_preferences: [] });
const models = ref([1, 2, 3]);

const remainingPoints = computed(() => {
    const total = models.value.reduce((acc, val) => acc + val, 0);
    return 10 - total;
});

const maxModelValue = (index) => {
    return models.value[index] + remainingPoints.value;
};

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
                highlightChartSection(i, false);
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
    margin-top: -60px;
    margin-bottom: -40px;
}

.model-container {
    background-color: #fff;
    border-radius: 8px;
    border: 3px solid #f0f0f0;
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
    border: 3px solid #f0f0f0;
    margin: 10px;
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.hover-border-topic {
    border: 3px dashed #215f99 !important;
}

.input-spacing {
    margin-left: 20px;
    margin-top: 10px;
}
.custom-slider-container{
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    margin: 20px;
}
.custom-slider-container .ant-slider {
    width: 80%;
    margin: 20px auto;
}

.custom-slider-container .ant-slider-mark-text {
    white-space: nowrap;
}
</style>
