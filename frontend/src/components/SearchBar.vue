<template>
    <div class="search-container">
        <el-input
          v-model="searchQuery"
          placeholder="Search"
          size="large"
        >
          <template #append>
            <el-button :icon="Search" @click="onSearch"/>
          </template>
        </el-input>

        <div class="buttons-container">
            <el-button :icon="Setting" size="large" class="icon-button" />
        </div>
    </div>
</template>

<script setup>
import { ref, defineEmits } from 'vue';
import { Search, Setting } from '@element-plus/icons-vue';

const searchQuery = ref('');
const emit = defineEmits(['search']);
const onSearch = () => {
  emit('search', searchQuery.value);
};
</script>

<style scoped>
.search-container {
    display: flex;
    align-items: center;
}

.round-input .el-input__inner {
    border-radius: 50%;
    height: 100%;
    padding: 0 8px;
}

.icon-button {
    margin-left: 5px;
}
</style>
