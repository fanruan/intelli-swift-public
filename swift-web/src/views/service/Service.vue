<template>
    <section>
        <el-table
                :data="services"
                highlight-current-row
                v-loading="listLoading"
                @selection-change="selsChange"
                style="width: 100%;"
        >
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column type="index" width="60"></el-table-column>
            <el-table-column prop="id" label="id" width="200" sortable></el-table-column>
            <el-table-column prop="clusterId" label="clusterId" width="120" sortable></el-table-column>
            <el-table-column prop="service" label="service" width="180" sortable></el-table-column>
            <el-table-column prop="serviceInfo" label="serviceInfo" min-width="100" sortable></el-table-column>
            <el-table-column prop="singleton" label="singleton" min-width="200" sortable></el-table-column>
        </el-table>
    </section>
</template>

<script>

    export default {
        data() {
            return {
                services: []
            };
        },
        methods: {
            getAllServices() {
                this.$axios
                    .get("/api/service/query")
                    .then(successResponse => {
                        this.services = successResponse.data.data;
                    })
                    .catch(failResponse => {
                        console.log(JSON.stringify(failResponse));
                    });
            }
        },
        mounted() {
            this.getAllServices();
        }
    };
</script>

<style scoped>
</style>