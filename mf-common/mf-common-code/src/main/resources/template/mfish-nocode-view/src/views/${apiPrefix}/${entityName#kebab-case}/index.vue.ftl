<!--
 @description: ${tableInfo.tableComment}
 @author: mfish
 @date: ${.now?string["yyyy-MM-dd"]}
 @version: V1.2.0
-->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate" v-auth="'sys:${entityName?uncap_first}:insert'">新增</a-button>
        <a-button type="error" @click="handleExport" v-auth="'sys:${entityName?uncap_first}:export'">导出</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction
                  :actions="[
              {
                icon: 'ant-design:edit-outlined',
                onClick: handleEdit.bind(null, record),
                auth: 'sys:${entityName?uncap_first}:update',
                tooltip: '修改'
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                popConfirm: {
                  title: '是否确认删除',
                  placement: 'left',
                  confirm: handleDelete.bind(null, record)
                },
                auth: 'sys:${entityName?uncap_first}:delete',
                tooltip: '删除'
              }
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <${entityName}Modal @register="registerModal" @success="handleSuccess" />
  </div>
</template>
<script lang="ts" setup>
  import { BasicTable, useTable, TableAction } from "/@/components/general/Table";
  import { delete${entityName}, export${entityName}, get${entityName}List } from "/@/api/${apiPrefix}/${entityName}";
  import { useModal } from "/@/components/general/Modal";
  import ${entityName}Modal from "./${entityName}Modal.vue";
  import { columns, searchFormSchema } from "./${entityName?uncap_first}.data";
  import { ${entityName} } from "/@/api/${apiPrefix}/model/${entityName}Model";

  defineOptions({ name: "${entityName}Management" });
  const [registerModal, { openModal }] = useModal();
  const [registerTable, { reload, getForm }] = useTable({
    title: "${tableInfo.tableComment}列表",
    api: get${entityName}List,
    columns,
    formConfig: {
      name: "search_form_item",
      labelWidth: 100,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true
    },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: false,
    actionColumn: {
      width: 80,
      title: "操作",
      dataIndex: "action"
    }
  });

  /**
   * 新建
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false
    });
  }

  /**
   *  导出自动生成支持导出1000条可自行修改
   */
  function handleExport() {
    export${entityName}({ ...getForm().getFieldsValue(), pageNum: 1, pageSize: 1000 });
  }

  /**
   * 修改
   * @param ${entityName?uncap_first} ${tableInfo.tableComment}对象
   */
  function handleEdit(${entityName?uncap_first}: ${entityName}) {
    openModal(true, {
      record: ${entityName?uncap_first},
      isUpdate: true
    });
  }

  /**
   * 删除
   * @param ${entityName?uncap_first} ${tableInfo.tableComment}对象
   */
  function handleDelete(${entityName?uncap_first}: ${entityName}) {
    if(${entityName?uncap_first}.id){
      delete${entityName}(${entityName?uncap_first}.id).then(() => {
        handleSuccess();
      });
    }
  }

  /**
   * 处理完成
   */
  function handleSuccess() {
    reload();
  }
</script>
