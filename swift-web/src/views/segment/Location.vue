<template>
  <section>
    <el-table
      :data="locations"
      highlight-current-row
      v-loading="listLoading"
      @selection-change="selsChange"
      style="width: 100%;"
    >
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column type="index" width="60"></el-table-column>
      <el-table-column prop="clusterId" label="clusterId" width="200" sortable></el-table-column>
      <el-table-column prop="segmentId" label="segmentId" width="400" sortable></el-table-column>
      <el-table-column prop="sourceKey" label="sourceKey" width="400" sortable></el-table-column>
    </el-table>
  </section>
</template>

<script>
import util from "../../common/js/util";

export default {
  data() {
    return {
      locations: []
    };
  },
  methods: {
    getAllLocations() {
      this.$axios
        .get("/api/segment/location/query")
        .then(successResponse => {
          this.locations = successResponse.data.data;
        //   data.forEach(element => {
        //     var segment = {
        //       id: element.id,
        //       order: element.order,
        //       sourceKey: element.sourceKey,
        //       swiftSchema: element.swiftSchema
        //     };
        //     this.segments.push(segment);
        //   });
        })
        .catch(failResponse => {
          console.log(JSON.stringify(failResponse));
        });
    }
  },
  mounted() {
    this.getAllLocations();
  }
};
</script>

<style scoped>
</style>