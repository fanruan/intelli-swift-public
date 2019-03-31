<template>
    <section>
        <common-table :meta="meta" get="/api/segment/query"></common-table>
    </section>
</template>

<script>
    import CommonTable from '../../components/CommonTable.vue'
    export default {
        components: {
            CommonTable
        },
        data() {
            return {
                segments: [],
                meta: {
                    columns: [
                        {
                            prop: 'id',
                            label: 'id',
                            width: 400,
                            sort: true,
                            expand: false
                        },{
                            prop: 'order',
                            label: 'order',
                            width: 120,
                            sort: true,
                            expand: false
                        },{
                            prop: 'sourceKey',
                            label: 'sourceKey',
                            width: 200,
                            sort: true,
                            expand: false
                        },{
                            prop: 'swiftSchema',
                            label: 'swiftSchema',
                            minWidth: 200,
                            sort: true,
                            expand: false
                        }
                    ]
                }
            };
        },
        methods: {
        },
        mounted() {
        }
    };
</script>

<style scoped>
</style>