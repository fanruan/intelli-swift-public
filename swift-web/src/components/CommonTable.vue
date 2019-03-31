<template>
    <section>

		<!--列表-->
		<el-table :data="tableData" highlight-current-row v-loading="isLoading" @selection-change="selectChanged" style="width: 100%;">
			<el-table-column type="selection" width="55">
			</el-table-column>
			<el-table-column type="index" width="60">
			</el-table-column>
			<el-table-column 
      v-for="item in meta.columns" :key="item.prop" 
      :prop="item.prop" :label="item.label" :width="item.width" 
      :min-width="null == item.minWidth ? item.width:item.minWidth" :sortable="item.sort" :type="item.expand? 'expand' : ''">
        <template slot-scope="props">
          <el-table :data="props.row[item.prop]" v-if="item.expand">
            <el-table-column v-for="child in item.children" :key="child.prop" :prop="child.prop" :label="child.label" :width="child.width" :sortable="child.sort">
            </el-table-column>
          </el-table>
          <div class="cell" v-if="!item.expand">{{props.row[item.prop]}}</div>
        </template>
        
			</el-table-column>
			<el-table-column label="操作" width="200" v-if="null != meta.rowOp && undefined != meta.rowOp && meta.rowOp.length > 0">
				<el-button-group slot-scope="scope">
					<el-button size="small" :type="btn.type" @click="$emit(btn.evt, scope.row)" v-for="btn in meta.rowOp" :key="btn.evt">{{btn.name}}</el-button>
					<!-- <el-button type="danger" size="small" @click="$emit('row-del', scope.row)">删除</el-button> -->
				</el-button-group>
			</el-table-column>
		</el-table>

		<!--工具条-->
		<el-col :span="24" class="toolbar">
			<el-button type="danger" @click="$emit('batch-del', sels)" :disabled="sels.length===0">批量删除</el-button>
			<el-pagination layout="prev, pager, next" @current-change="changePage" :page-size="pageSize" :total="total" style="float:right;">
			</el-pagination>
		</el-col>

    </section>
</template>
<script>

import axios from 'axios';
import qs from 'qs'

let get = (url, params) => { return axios.get(url, {params: params}); }
let post = (url, data) => { return axios.post(url, qs.stringify(data)); }

export default {
  name:'CommonTable',
  props: {
    get: String,
    post: String,
    pageSize:{ 
      type: Number,
      required: true
    },
    meta: {
      type: Object,
      required: true
    },
    data: Array
  },
  data() {
    return {
      tableData: [],
      total: 0,
      page: 1,
      isLoading: false,
      sels: [],
      filter: null
    };
  },
  methods: {
      selectChanged: function(sels) {
          this.sels = sels
      },
      loadData: function(param) {
        if (null != this.data) {
          this.tableData = this.data
        } else {
          const _this = this;
          if (null != param) {
            param.page = this.page
            param.size = this.pageSize
          } else{
            param = {
              page: this.page,
              size: this.pageSize
            }
          }
          
          this.isLoading = true;
          if (null != this.get && undefined != this.get) {
            get(this.get, param).then(res => {
              _this.tableData = res.data.data
              if (null == res.data.total || undefined == res.data.total) {
                _this.total = _this.tableData.length
              } else {
                _this.total = res.data.total
              }
              _this.isLoading = false
            }).catch((err)=>{
              _this.isLoading = false
              _this.total = 0
              this.$message({
                message: '加载失败: ' + err.message,
                type: 'error'
              });
            })
          } else if(null != this.post && undefined != this.post) {
            post(this.post, param).then(res => {
              _this.tableData = res.data.data
              if (null == res.data.total || undefined == res.data.total) {
                _this.total = _this.tableData.length
              } else {
                _this.total = res.data.total
              }
              _this.isLoading = false
            }).catch((err) => {
              this.$message({
                message: '加载失败: ' + err.message,
                type: 'error'
              });
            })
          } else {
            this.$message({
                message: '加载失败: 请填写get 或 post属性',
                type: 'error'
              });
          }
        }
      },
      changePage: function(val) {
          this.page = val
          if (null != this.data && this.data.length > 0) {
            let filterData = this.data;
            if (null != this.filter) {
              let keys = Object.keys(this.filter)
              filterData = this.data.filter(obj => {
                for (i in keys) {
                  if (!(filter[keys[i]] && filter[keys[i]] == obj[keys[i]])) {
                    return false
                  }
                }
                return true;
              })
            }
            let total = filterData.length
            tableData = filterData.filter((u, index) => index < pageSize * page && index >= pageSize * (page - 1));
          } else {
            this.loadData(this.filter)
          }
      },
      reloadData: function(param) {
        this.page = 1
        this.filter = param
        this.loadData(param)
      }
  },
  computed: {
      

  },
  mounted() {
    this.loadData(null)
  }
};
</script>