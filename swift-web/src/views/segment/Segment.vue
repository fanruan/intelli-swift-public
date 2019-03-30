<template>
    <section>
        <el-table
                :data="segments"
                highlight-current-row
                v-loading="listLoading"
                @selection-change="selsChange"
                style="width: 100%;"
        >
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column type="index" width="60"></el-table-column>
            <el-table-column prop="id" label="id" width="400" sortable></el-table-column>
            <el-table-column prop="order" label="order" width="120" sortable></el-table-column>
            <el-table-column prop="sourceKey" label="sourceKey" width="200" sortable></el-table-column>
            <el-table-column prop="swiftSchema" label="swiftSchema" min-width="100" sortable></el-table-column>
        </el-table>
    </section>
</template>

<script>

    export default {
        data() {
            return {
                segments: []
            };
        },
        methods: {
            getAllSegments() {
                this.$axios
                    .get("/api/segment/query")
                    .then(successResponse => {
                        var data = successResponse.data.data;
                        data.forEach(element => {
                            var segment = {
                                id: element.id,
                                order: element.order,
                                sourceKey: element.sourceKey,
                                swiftSchema: element.swiftSchema
                            };
                            this.segments.push(segment);
                        });
                    })
                    .catch(failResponse => {
                        console.log(JSON.stringify(failResponse));
                    });
            }
        },
        mounted() {
            this.getAllSegments();
        }
    };
</script>

<style scoped>
</style>