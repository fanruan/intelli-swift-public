<template>
    <section>
        <el-col :span="24" class="toolbar" style="padding-bottom: 0px;">
            <el-form :inline="true" :model="filters">
                <el-form-item>
                    <el-input v-model="filters.name" placeholder="表名"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" v-on:click="getTableByName()">查询</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="showCreateTable()">创建</el-button>
                </el-form-item>
            </el-form>
        </el-col>

        <el-table
                :data="tables"
                highlight-current-row
                style="width: 100%;"
        >
            <el-table-column type="index"></el-table-column>
            <el-table-column prop="id" label="id" id="id" sortable></el-table-column>
            <el-table-column label="fields" id="fields" width="140" sortable type="expand">
                <template slot-scope="props">
                    <el-table :data="props.row.fields">
                        <el-table-column prop="name" label="name" id="name"></el-table-column>
                        <el-table-column prop="type" label="type" id="type"></el-table-column>
                        <el-table-column prop="remark" label="remark" id="remark"></el-table-column>
                        <el-table-column prop="precision" label="precision" id="precision"></el-table-column>
                        <el-table-column prop="scale" label="scale" id="scale"></el-table-column>
                        <el-table-column prop="columnId" label="columnId" id="columnId"></el-table-column>
                    </el-table>
                </template>
            </el-table-column>
            <el-table-column prop="remark" label="remark" sortable></el-table-column>
            <el-table-column prop="swiftDatabase" label="swiftSchema" sortable></el-table-column>
            <el-table-column prop="schemaName" label="schemaName" sortable></el-table-column>
            <el-table-column prop="tableName" label="tableName" sortable></el-table-column>
            <el-table-column label="操作">
                <template slot-scope="scope">
                    <el-button size="small" @click="handleEdit(scope.$index, scope.row)">导入</el-button>
                    <el-button type="danger" size="small" @click="handleDel(scope.$index, scope.row)">预览</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!--新增界面-->
        <el-dialog title="新建表" :visible.sync="createFormVisible" :close-on-click-modal="false">
            <table>
                <tr>
                    <td>字段名</td>
                    <td>类型</td>
                    <td>长度</td>
                    <td>小数点</td>
                    <td>操作</td>
                </tr>
                <tr>
                    <td>
                        <el-input v-model="addedField.addFieldName"></el-input>
                    </td>
                    <td>
                        <el-select class="item-choose" v-model="addedField.addFieldType" placeholder="请选择字段类型">
                            <el-option
                                    v-for="item in types"
                                    v-bind:key="item.key"
                                    :value="item.value"
                                    :label="item.key"
                            ></el-option>
                        </el-select>
                    </td>
                    <td>
                        <el-input v-model="addedField.addPrecision"></el-input>
                    </td>
                    <td>
                        <el-input v-model="addedField.addScale"></el-input>
                    </td>
                    <td>
                        <el-button @click.native="addField">添加</el-button>
                    </td>
                </tr>
                <tr v-for="(item,index) in calcAddFields" :key="item">
                    <td>{{item.addFieldName}}</td>
                    <td>{{item.addFieldType}}</td>
                    <td>{{item.addPrecision}}</td>
                    <td>{{item.addScale}}</td>
                    <td>
                        <el-button @click.native="removeField(index)">删除</el-button>
                    </td>
                </tr>
            </table>
            <div slot="footer" class="dialog-footer">
                <el-button @click.native="cancelSubmit">取消</el-button>
                <el-button type="primary" @click.native="addSubmit" :loading="addLoading">提交</el-button>
            </div>
        </el-dialog>
        <!-- <el-dialog title="导入表" v-model="createFormVisible" :close-on-click-modal="false"> -->


    </section>
</template>

<script>

    export default {
        data() {
            return {
                filters: {
                    name: ""
                },
                //show tables
                tables: [],
                addLoading: false,
                //create table
                addFields: [],
                addedTableName: "",
                addedField: {
                    addFieldName: "",
                    addFieldType: "",
                    addPrecision: "",
                    addScale: ""
                },

                createFormVisible: false,
                types: [
                    {key: "integer", value: 4},
                    {key: "long", value: -5},
                    {key: "double", value: 8},
                    {key: "date", value: 91},
                    {key: "string", value: 12}
                ]
            };
        },
        computed: {
            calcAddFields() {
                return this.addFields;
            }
        },
        methods: {
            getAllTables() {
                this.$axios
                    .get("/api/table/query")
                    .then(successResponse => {
                        this.tables = successResponse.data.data;
                    })
                    .catch(failResponse => {
                        console.log(JSON.stringify(failResponse));
                    });
            },
            getTableByName: function () {
                this.$axios
                    .get("/api/table/query?tableName=" + this.filters.name)
                    .then(successResponse => {
                        this.tables = successResponse.data.data;
                    })
                    .catch(failResponse => {
                        console.log(JSON.stringify(failResponse));
                    });
            },
            showCreateTable: function () {
                this.createFormVisible = true;
            },
            addField: function () {
                var field = {
                    addFieldName: this.addedField.addFieldName,
                    addFieldType: this.addedField.addFieldType,
                    addPrecision: this.addedField.addPrecision,
                    addScale: this.addedField.addScale
                };
                if (this.checkAddedField()) {
                    alert("屬性不得为空");
                } else {
                    this.addFields.push(field);
                    this.clearAddedField();
                }
            },
            removeField: function (index) {
                var tmpFields = this.addFields;
                this.addFields = [];
                for (let i = 0; i < tmpFields.length; i++) {
                    if (index == i) {
                        continue;
                    }
                    this.addFields.push(tmpFields[i]);
                }
            },
            cancelSubmit: function () {
                this.createFormVisible = false;
                this.addFields = [];
                this.clearAddedField();
            },
            addSubmit: function () {
                var tableName = prompt("请输入表名");
                this.addLoading = true;
                let requestBody = {tableName: tableName, addFields: this.addFields};
                this.$axios
                    .post("/api/table/create", requestBody)
                    .then(successResponse => {
                        var result = successResponse.data.data;
                        if (result) {
                            this.createFormVisible = false;
                            alert("Create table succeed!");
                        } else {
                            alert("Create table failed!Please check!");
                        }
                    })
                    .catch(failResponse => {
                        console.log(JSON.stringify(failResponse));
                    });
                this.addLoading = false;
            },
            clearAddedField: function () {
                this.addedField.addFieldName = "";
                this.addedField.addFieldType = "";
                this.addedField.addPrecision = "";
                this.addedField.addScale = "";
            },
            checkAddedField: function () {
                if (
                    this.addedField.addFieldName == "" ||
                    this.addedField.addFieldType == "" ||
                    this.addedField.addPrecision == "" ||
                    this.addedField.addScale == ""
                ) {
                    return true;
                } else {
                    return false;
                }
            }
        },
        mounted() {
            this.getAllTables();
        }
    };
</script>

<style scoped>
</style>