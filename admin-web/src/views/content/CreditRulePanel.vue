<template>
  <div class="credit-panel">
    <div class="panel-title">
      <i :class="['fas', type === 'add' ? 'fa-plus-circle' : 'fa-minus-circle']"></i>
      {{ title }}
      <span :class="['count-tag', type]">已配置 {{ rows.length }} 条</span>
    </div>
    <div class="credit-table-wrap">
      <table>
        <thead><tr><th>#</th><th>规则项</th><th>规则描述</th><th>规则触发时机</th><th>分值</th><th>执行次数</th><th>数据更新时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="(row, index) in rows" :key="index" :class="{ editing: editingIndex === index }">
            <td>{{ index + 1 }}</td>
            <template v-if="editingIndex === index">
              <td><input v-model="draft.name"></td><td><textarea v-model="draft.desc"></textarea></td><td><input v-model="draft.trigger"></td>
              <td><input v-model.number="draft.score" class="score-input" type="number" min="1"></td><td><input v-model="draft.times"></td><td><input v-model="draft.updateTime"></td>
              <td class="actions"><button class="save" @click="save(index)">保存</button><button @click="editingIndex = -1">取消</button></td>
            </template>
            <template v-else>
              <td>{{ row.name }}</td><td>{{ row.desc || '-' }}</td><td>{{ row.trigger || '-' }}</td>
              <td><span :class="['score', type]">{{ type === 'add' ? '+' : '' }}{{ row.score }}</span></td>
              <td>{{ row.times }}</td><td>{{ row.updateTime }}</td>
              <td class="actions"><button @click="edit(index)">编辑</button><button class="danger" @click="$emit('remove', index)">删除</button></td>
            </template>
          </tr>
        </tbody>
      </table>
    </div>
    <button class="add-row" @click="add"><i class="fas fa-plus"></i> 新增{{ type === 'add' ? '加分' : '减分' }}规则</button>
  </div>
</template>

<script setup>
import { nextTick, reactive, ref } from 'vue'
const props = defineProps({ title: String, rows: { type: Array, default: () => [] }, type: String })
const emit = defineEmits(['add', 'remove'])
const editingIndex = ref(-1)
const draft = reactive({ name: '', desc: '', trigger: '', score: 1, times: '', updateTime: '' })
const edit = (index) => { Object.assign(draft, props.rows[index], { score: Math.abs(props.rows[index].score) }); editingIndex.value = index }
const save = (index) => { Object.assign(props.rows[index], draft, { score: props.type === 'add' ? Math.abs(draft.score || 1) : -Math.abs(draft.score || 1) }); editingIndex.value = -1 }
const add = async () => { emit('add'); await nextTick(); edit(props.rows.length - 1) }
</script>

<style scoped>
.credit-panel{background:#fff;border:1px solid #eee;border-radius:12px;padding:20px;min-width:0}.panel-title{display:flex;align-items:center;gap:8px;font-size:15px;font-weight:700;margin-bottom:14px}.panel-title i{color:#ff6b35}.count-tag{font-size:11px;padding:2px 8px;border-radius:10px;font-weight:500}.count-tag.add,.score.add{background:#fff7f3;color:#ff6b35}.count-tag.sub,.score.sub{background:#eef2ff;color:#4f46e5}.credit-table-wrap{overflow-x:auto}table{width:100%;border-collapse:collapse;font-size:12px;min-width:760px}th{background:#fafafa;color:#666;font-weight:600;text-align:left;padding:10px 8px;white-space:nowrap}td{padding:10px 8px;border-bottom:1px solid #f0f0f0;color:#333}.score{display:inline-block;padding:2px 9px;border-radius:10px;font-weight:600}.actions{white-space:nowrap}.actions button{border:0;background:none;color:#4f46e5;cursor:pointer;padding:3px 6px}.actions .danger{color:#ff5c33}.actions .save{color:#10b981}.editing td{background:#fffbf5}.editing input,.editing textarea{width:100%;box-sizing:border-box;border:1px solid #ff6b35;border-radius:5px;padding:5px;font:inherit}.editing textarea{min-height:44px}.editing .score-input{width:58px}.add-row{width:100%;padding:12px;margin-top:14px;border:1.5px dashed #d0d0d0;border-radius:10px;background:#fafafa;color:#999;cursor:pointer}.add-row:hover{border-color:#ff6b35;color:#ff6b35;background:#fff7f3}
</style>
