(function () {
  const segments = location.pathname.split('/').filter(Boolean)
  const roleIndex = segments.indexOf('prototype') + 1
  const role = segments[roleIndex]
  const page = (segments[roleIndex + 1] || '').replace(/\.html$/, '')
  function fitViewport() {
    const frame = document.querySelector('.phone-frame')
    if (frame) Object.assign(frame.style, {width:'100%',maxWidth:'430px',height:'100dvh',minHeight:'100dvh',border:'0',borderRadius:'0',boxShadow:'none',margin:'0 auto',boxSizing:'border-box',paddingTop:'env(safe-area-inset-top)'})
    Object.assign(document.documentElement.style,{width:'100%',minHeight:'100%',background:'#fff8e6'})
    Object.assign(document.body.style,{width:'100%',minHeight:'100%',margin:'0',background:'#fff8e6'})
  }
  function removeDeviceChrome() {
    const style = document.createElement('style')
    style.textContent = '.dynamic-island,.status-bar,.nav-right,.page-indicator{display:none!important}.top-nav{position:relative!important;justify-content:flex-start!important;padding-top:env(safe-area-inset-top)!important;box-sizing:border-box!important}.top-nav .brand-tag{position:absolute!important;left:50%!important;transform:translateX(-50%)!important;margin:0!important}'
    document.head.appendChild(style)
  }
  const typeMap={message:'MESSAGE','history-message':'MESSAGE',notification:'NOTIFICATION',wallet:'WALLET',record:'WALLET_LEDGER',intent:'JOB_INTENT','intent-job':'JOB_INTENT','intent-personal':'JOB_INTENT','intent-preference':'JOB_INTENT',appeal:'APPEAL','appeal-submit':'APPEAL',report:'REPORT',credit:'CREDIT','star-level':'CREDIT',coupon:'COUPON',invite:'INVITE',classroom:'COURSE','course-video':'COURSE','course-order':'COURSE_ORDER','course-rule':'RULE','task-training':'TASK','task-detail':'TASK',rule:'RULE','rule-detail':'RULE','user-agreement':'AGREEMENT',privacy:'PRIVACY',service:'FAQ','service-chat':'SERVICE_CHAT',realname:'CERTIFICATION','user-info':'PROFILE',badge:'BADGE',deposit:'DEPOSIT',banner:'BANNER','boss-message':'MESSAGE','all-jobs':'RECRUIT_TEMPLATE','recruit-manager':'RECRUIT_TEMPLATE','recruit-settings':'RECRUIT_SETTING','recruit-demand':'RECRUIT_TEMPLATE','recruit-address':'ADDRESS','search-worker':'TALENT','talent-list':'TALENT','sub-account':'SUB_ACCOUNT','enterprise-cert':'ENTERPRISE_CERT','enterprise-cert-form':'ENTERPRISE_CERT','personal-info':'PROFILE','applicant-info':'APPLICANT',settlement:'SETTLEMENT','expense-detail':'EXPENSE','payment-detail':'PAYMENT',voucher:'COUPON',contract:'CONTRACT',insurance:'INSURANCE',points:'POINTS',reward:'REWARD','invite-worker':'INVITE','invite-friend':'INVITE','invite-code':'INVITE',blacklist:'BLACKLIST','system-notice':'NOTIFICATION','creditor-score':'CREDIT','boss-employer':'ENTERPRISE_CERT','schedule-stats':'SCHEDULE','signup-notice':'NOTICE','task-content':'TASK','settle-confirm':'SETTLEMENT','suspend-settle':'SETTLEMENT'}
  Object.assign(typeMap,{follow:'FOLLOW','follow-qrcode':'FOLLOW_QRCODE','join-group':'JOIN_GROUP',copyright:'COPYRIGHT',download:'DOWNLOAD','faq-detail':'FAQ_DETAIL',search:'SEARCH',filter:'FILTER','monthly-order':'MONTHLY_ORDER','monthly-salary':'MONTHLY_SALARY','press-order':'PRESS_ORDER','press-salary':'PRESS_SALARY','simulate-order':'SIMULATE_ORDER','switch-identity':'SWITCH_IDENTITY','add-address':'ADD_ADDRESS','backup-phone':'BACKUP_PHONE','boss-filter':'BOSS_FILTER','download-app':'DOWNLOAD_APP','gender-age':'GENDER_AGE',location:'LOCATION','missed-call':'MISSED_CALL','publish-info':'PUBLISH_INFO','publish-success':'PUBLISH_SUCCESS',realname:'CERTIFICATION','select-job':'SELECT_JOB','select-work-time':'SELECT_WORK_TIME','switch-account':'SWITCH_ACCOUNT'})
  const publicTypes=new Set(['BANNER','NOTICE','RULE','AGREEMENT','PRIVACY','COURSE','FAQ'])
  function asRows(data){return Array.isArray(data)?data:(data?[data]:[])}
  function markLoaded(data){document.documentElement.dataset.apiLoaded='true';document.documentElement.dataset.apiCount=String(asRows(data).length)}
  function applyApiData(data){
    const rows=asRows(data);if(!rows.length)return
    if(page==='job-detail'&&!Array.isArray(data)){
      const set=(selector,value)=>{const el=document.querySelector(selector);if(el&&value!=null)el.textContent=value}
      set('.salary-title',data.title);set('.salary-value-gray',`¥${data.unitPrice}/${data.salaryType==='HOURLY'?'小时':'天'}`)
      const infoValues=document.querySelectorAll('.info-value');if(infoValues[0])infoValues[0].textContent=`${data.startTime||''} 至 ${data.endTime||''}`;if(infoValues[1])infoValues[1].textContent=data.address||''
      const desc=document.querySelector('.desc-text');if(desc&&data.description)desc.textContent=data.description
      const count=document.querySelector('.enroll-count');if(count)count.textContent=`剩${Math.max(0,(data.headcount||0)-(data.hiredCount||0))}个名额`
    }
    const titleSelectors='.card-title,.item-title,.message-title,.notice-title,.rule-title,.course-title,.task-title,.faq-title,.coupon-title,.job-title,.record-title,.list-title'
    const contentSelectors='.card-content,.item-content,.message-content,.notice-content,.rule-content,.course-desc,.task-desc,.faq-content,.description,.desc'
    const statusSelectors='.status,.status-text,.item-status,.tag'
    const titles=[...document.querySelectorAll(titleSelectors)].filter(el=>!el.closest('header'))
    const contents=[...document.querySelectorAll(contentSelectors)]
    const statuses=[...document.querySelectorAll(statusSelectors)]
    rows.forEach((row,index)=>{
      if(titles[index]&&row.title)titles[index].textContent=row.title
      if(contents[index]&&row.content)contents[index].textContent=row.content
      if(statuses[index]&&row.status)statuses[index].textContent=row.status
    })
    const first=rows[0];let json={};try{json=JSON.parse(first.dataJson||'{}')}catch{}
    const values={title:first.title,content:first.content,...json}
    document.querySelectorAll('input[name],textarea[name],select[name]').forEach(el=>{if(values[el.name]!=null)el.value=values[el.name]})
  }
  function showResult(message,ok=true){
    const toast=document.createElement('div');toast.textContent=message;Object.assign(toast.style,{position:'fixed',left:'50%',bottom:'90px',transform:'translateX(-50%)',zIndex:99999,padding:'10px 18px',borderRadius:'20px',background:ok?'rgba(0,0,0,.78)':'#e34d59',color:'#fff',fontSize:'14px'});document.body.appendChild(toast);setTimeout(()=>toast.remove(),1800)
  }
  function bindAppNavigation(){
    const workerPages=new Set(['home','profile','classroom','credit','monthly-order','monthly-salary','order','message','press-order','press-salary','task-detail','task-training','coupon','deposit','wallet','service','switch-identity','user-info','follow','record','badge','star-level','banner','filter','search','job-detail','simulate-order','course-order','course-rule','course-video','download','faq-detail','intent','intent-job','intent-personal','intent-preference','invite','join-group','notification','privacy','realname','rule','rule-detail','copyright','user-agreement','appeal','appeal-submit','report','history-message','service-chat','settlement'])
    const bossPages=new Set(['boss-employer','boss-home','boss-message','boss-order','boss-profile','boss-publish','search-worker','select-job','publish-info','publish-success','schedule-stats','enterprise-cert','enterprise-cert-form','creditor-score','talent-list','expense-detail','payment-detail','recruit-manager','recruit-address','recruit-demand','recruit-settings','sub-account','suspend-settle','settle-confirm','switch-account','invite-code','invite-worker','blacklist','all-jobs','boss-filter','settlement','contract','system-notice','missed-call','signup-notice','invite-friend','service-chat','insurance','realname','points','voucher','reward','add-address','download-app','personal-info','applicant-info','backup-phone','gender-age','location','task-content'])
    window.navigateTo=function(target){const [name,query='']=String(target||'').split('?');const targetRole=bossPages.has(name)?'boss':workerPages.has(name)?'worker':role;top.location.href=`/${targetRole}/${name}${query?`?${query}`:''}`}
    if(page==='switch-identity'){
      const current=new URLSearchParams(location.search).get('role')||'worker'
      window.doSwitch=function(){const next=current==='boss'?'worker':'boss';localStorage.setItem('role',next.toUpperCase());localStorage.setItem('userId',next==='boss'?'3001':'2001');top.location.href=next==='boss'?'/boss/home':'/worker/home'}
      const area=document.querySelector('.action-area,.content-area,.phone-frame');if(area&&!document.querySelector('.kuaima-logout')){const logout=document.createElement('button');logout.className='kuaima-logout';logout.textContent='退出登录';Object.assign(logout.style,{display:'block',width:'calc(100% - 32px)',margin:'18px 16px',padding:'12px',border:'1px solid #ff6b35',borderRadius:'24px',background:'#fff',color:'#ff4d4f',fontSize:'15px'});logout.onclick=()=>{localStorage.clear();top.location.href='/login'};area.appendChild(logout)}
    }
    if(page==='switch-account')document.addEventListener('click',event=>{const button=event.target.closest('button');if(button&&/退出登录/.test(button.textContent||'')){event.preventDefault();event.stopImmediatePropagation();localStorage.clear();top.location.href='/login'}},true)
  }
  function bindGenericSave(type,url,data){
    if(publicTypes.has(type)||['SETTLEMENT','PAYMENT','WALLET','WALLET_LEDGER'].includes(type))return
    const words=/保存|提交|确认修改|完成认证|立即开通/
    document.addEventListener('click',async event=>{
      const button=event.target.closest('button,.submit-btn,.save-btn,.confirm-btn,.primary-btn')
      if(!button||!words.test(button.textContent||''))return
      event.preventDefault();event.stopImmediatePropagation()
      const fields={};document.querySelectorAll('input,textarea,select').forEach((el,index)=>{fields[el.name||el.id||`field${index}`]=el.value})
      const rows=asRows(data);const body={title:fields.title||document.title,content:fields.content||fields.description||JSON.stringify(fields),dataJson:JSON.stringify(fields),status:'ACTIVE'}
      const target=rows[0]?.id?`${url}/${rows[0].id}`:url
      try{const response=await fetch(target,{method:rows[0]?.id?'PUT':'POST',headers:{'Content-Type':'application/json','X-User-Id':role==='boss'?'3001':'2001'},body:JSON.stringify(body)});const result=await response.json();if(result.code!=='0')throw new Error(result.message);showResult('保存成功')}catch(error){showResult(error.message||'保存失败',false)}
    },true)
  }
  async function loadApiData(){
    if(!['worker','boss'].includes(role))return
    const params=new URLSearchParams(location.search)
    if(role==='worker'&&page==='home'){
      try{
        const headers={'X-User-Id':'2001'}
        const [jobResult,orderResult]=await Promise.all([fetch('/api/v1/worker/jobs?pageNo=1&pageSize=20',{headers}).then(r=>r.json()),fetch('/api/v1/worker/orders',{headers}).then(r=>r.json())])
        const jobs=jobResult.data?.records||[],orders=orderResult.data||[],cards=[...document.querySelectorAll('.job-card')]
        const statusText={APPLIED:'已报名',HIRED:'已录用',ARRIVED:'已到岗',COMPLETED:'待结算',SETTLED:'已结算',CANCELLED:'已取消'}
        cards.forEach((card,index)=>{const job=jobs[index];if(!job){card.style.display='none';return}const title=card.querySelector('.font-bold.text-base,.font-semibold.text-sm');if(title)title.textContent=job.title;const wage=card.querySelector('.wage-tag');if(wage)wage.textContent=job.unitPrice;const values=card.querySelectorAll('.info-value');const display=[job.title,`${job.startTime||''} ~ ${job.endTime||''}`,job.address,`${job.unitPrice}元`,`${job.headcount||job.remainingCount||0}人`,`${job.hiredCount||0}人`];values.forEach((el,i)=>{if(display[i]!=null)el.textContent=display[i]});card.onclick=()=>location.href=`job-detail.html?id=${job.id}`;const button=card.querySelector('button');const order=orders.find(o=>String(o.jobId)===String(job.id));if(button){button.textContent=order?(statusText[order.status]||'已报名'):'立即报名';button.disabled=!!order;button.onclick=async event=>{event.stopPropagation();if(order)return;try{const response=await fetch(`/api/v1/worker/orders/apply/${job.id}`,{method:'POST',headers});const result=await response.json();if(result.code!=='0')throw new Error(result.message);button.textContent='已报名';button.disabled=true;showResult('报名成功')}catch(error){showResult(error.message||'报名失败',false)}}}})
        window.__KUAIMA_API_DATA__=jobs;markLoaded(jobs)
      }catch(error){document.documentElement.dataset.apiLoaded='false'}
      return
    }
    if(role==='worker'&&page==='order'){
      try{
        const headers={'X-User-Id':'2001'},result=await fetch('/api/v1/worker/orders',{headers}).then(r=>r.json()),source=result.data||[]
        const status={APPLIED:['applied','已报名','status-applied'],HIRED:['hired','已录用','status-hired'],ARRIVED:['arrived','已到岗','status-arrived'],COMPLETED:['done','待结算','status-done'],SETTLED:['done','已完成','status-done'],CANCELLED:['done','已取消','status-done']}
        const groups={day:[],press:[],monthly:[]},dateKeys=['yesterday','today','tomorrow','day-after-tomorrow']
        source.forEach(row=>{const mapped=status[row.status]||['done',row.status,'status-done'];const group=row.settlementType==='PRESS'?'press':row.settlementType==='MONTH'?'monthly':'day';const canCancel=['APPLIED','HIRED'].includes(row.status);const date=dateKeys[groups[group].length%dateKeys.length];groups[group].push({id:row.id,jobId:row.jobId,title:row.title,employer:'快马认证雇主',tags:[`<span class="order-tag tag-green">${row.settlementType}</span>`],date,status:mapped[0],statusText:mapped[1],statusClass:mapped[2],time:`${row.startTime||''} ~ ${row.endTime||''}`,location:row.address||'',distance:'',count:'1人',wage:row.unitPrice,unit:row.settlementType==='MONTH'?'元/月':'元/天',actions:`<button class="btn-small btn-primary" onclick="event.stopPropagation();top.location.href='/worker/order-detail/${row.id}'">查看详情</button>${canCancel?`<button class="btn-small btn-outline" onclick="cancelOrder('${row.id}', event)">取消报名</button>`:''}`})})
        const fixedDates=['8/19','8/20','8/21','8/22'],dateNodes=[...document.querySelectorAll('*')].filter(el=>el.children.length===0&&fixedDates.includes(el.textContent.trim()))
        dateNodes.forEach((el,index)=>{const date=new Date();date.setDate(date.getDate()+index-1);el.textContent=`${date.getMonth()+1}/${date.getDate()}`})
        window.__KUAIMA_API_DATA__=source;window.__KUAIMA_ORDER_GROUPS__=groups
        window.eval("ordersData.day.splice(0,ordersData.day.length,...window.__KUAIMA_ORDER_GROUPS__.day);ordersData.press.splice(0,ordersData.press.length,...window.__KUAIMA_ORDER_GROUPS__.press);ordersData.monthly.splice(0,ordersData.monthly.length,...window.__KUAIMA_ORDER_GROUPS__.monthly);renderOrders();")
        let apiOrderType='day',apiDateFilter='all',apiStatusFilter='all'
        const renderApiOrders=()=>{let orders=groups[apiOrderType]||[];if(apiDateFilter!=='all')orders=orders.filter(order=>order.date===apiDateFilter);if(apiStatusFilter!=='all')orders=orders.filter(order=>order.status===apiStatusFilter);const list=document.getElementById('orderList');if(!orders.length){list.innerHTML='<div class="empty-state"><div class="text-[80px] mb-4">📋</div><div class="text-gray-400 text-sm">暂无相关订单</div></div>';return}const typeClass=apiOrderType==='day'?'type-day':apiOrderType==='press'?'type-press':'type-monthly',typeText=apiOrderType==='day'?'日结':apiOrderType==='press'?'压薪日结':'月结';list.innerHTML=orders.map(order=>`<div class="order-card" onclick="top.location.href='/worker/order-detail/${order.id}'"><div class="order-header"><div><div class="order-title"><span class="order-type-badge ${typeClass}">${typeText}</span>${order.title}</div><div class="order-tags">${order.tags.join('')}</div></div><span class="order-status ${order.statusClass}">${order.statusText}</span></div><div class="order-info"><div class="info-item"><i class="fas fa-clock"></i><span class="info-label">时间：</span><span class="info-value">${order.time}</span></div><div class="info-item"><i class="fas fa-map-marker-alt"></i><span class="info-label">地点：</span><span class="info-value">${order.location}</span></div><div class="info-item"><i class="fas fa-user"></i><span class="info-label">雇主：</span><span class="info-value">${order.employer}</span></div><div class="info-item"><i class="fas fa-walking"></i><span class="info-label">距离：</span><span class="info-value">${order.distance}</span></div></div><div class="order-footer"><div><span class="order-wage">¥${order.wage}</span><span class="order-wage-unit">${order.unit}</span></div><div class="order-actions">${order.actions}</div></div></div>`).join('')}
        window.filterByDate=function(date){apiDateFilter=date;document.querySelectorAll('#dayTabs .day-tab').forEach(tab=>tab.classList.toggle('active',(tab.dataset.filter||'')===date));renderApiOrders()}
        window.filterByStatus=function(status,element){apiStatusFilter=status;document.querySelectorAll('.status-tab').forEach(tab=>tab.classList.remove('active'));if(element)element.classList.add('active');renderApiOrders()}
        window.switchOrderType=function(type){apiOrderType=type;apiDateFilter='all';apiStatusFilter='all';document.querySelectorAll('.order-type-switch').forEach(tab=>{const active=tab.dataset.type===type;tab.classList.toggle('bg-white',active);tab.classList.toggle('font-semibold',active);tab.classList.toggle('text-gray-800',active);tab.classList.toggle('bg-white/50',!active);tab.classList.toggle('text-gray-600',!active)});document.querySelectorAll('#dayTabs .day-tab').forEach(tab=>tab.classList.toggle('active',tab.dataset.filter==='all'));document.querySelectorAll('.status-tab').forEach(tab=>tab.classList.toggle('active',tab.dataset.status==='all'));renderApiOrders()}
        document.querySelectorAll('#dayTabs .day-tab').forEach(tab=>{tab.dataset.filter=(tab.getAttribute('onclick')||'').match(/'([^']+)'/)?.[1]||'all';tab.removeAttribute('onclick');tab.onclick=()=>window.filterByDate(tab.dataset.filter)})
        document.querySelectorAll('.status-tab').forEach(tab=>{tab.dataset.status=(tab.getAttribute('onclick')||'').match(/'([^']+)'/)?.[1]||'all';tab.removeAttribute('onclick');tab.onclick=()=>window.filterByStatus(tab.dataset.status,tab)})
        document.querySelectorAll('[onclick^="switchOrderType"]').forEach(tab=>{tab.classList.add('order-type-switch');tab.dataset.type=(tab.getAttribute('onclick')||'').match(/'([^']+)'/)?.[1]||'day';tab.removeAttribute('onclick');tab.onclick=()=>window.switchOrderType(tab.dataset.type)})
        renderApiOrders();document.documentElement.dataset.orderBindings='true'
        const originalNavigate=window.navigateTo;window.navigateTo=function(pageName,event){if(pageName==='job-detail'){if(event)event.stopPropagation();const card=(event||window.event)?.target?.closest?.('.order-card');const title=card?.querySelector('.order-title')?.textContent||'';const row=source.find(item=>title.includes(item.title));if(row){top.location.href=`/worker/order-detail/${row.id}`;return}}return originalNavigate(pageName,event)}
        window.cancelOrder=async function(id,event){if(event)event.stopPropagation();if(!confirm('确定取消报名？'))return;try{const response=await fetch(`/api/v1/worker/orders/${id}/cancel`,{method:'POST',headers});const cancelResult=await response.json();if(cancelResult.code!=='0')throw new Error(cancelResult.message);showResult('已取消报名');setTimeout(()=>location.reload(),500)}catch(error){showResult(error.message||'取消失败',false)}}
        document.querySelectorAll('.order-card').forEach((card,index)=>{const row=source[index];if(row)card.onclick=()=>top.location.href=`/worker/order-detail/${row.id}`})
        markLoaded(source)
      }catch(error){document.documentElement.dataset.apiLoaded='false';console.error('Order prototype API load failed',error)}
      return
    }
    if(role==='worker'&&page==='settlement'){
      try{
        const headers={'X-User-Id':'2001'}
        const settlementResult=await fetch('/api/v1/worker/settlements',{headers}).then(r=>r.json())
        const settlement=(settlementResult.data||[])[0]
        if(!settlement)throw new Error('暂无结算数据')
        const [orderResult,walletResult]=await Promise.all([
          fetch(`/api/v1/worker/orders/${settlement.orderId}`,{headers}).then(r=>r.json()),
          fetch('/api/v1/worker/wallet',{headers}).then(r=>r.json())
        ])
        const order=orderResult.data||{},wallet=walletResult.data||{}
        const set=(selector,value)=>{const el=document.querySelector(selector);if(el&&value!=null)el.textContent=value}
        set('.settle-title',order.title||settlement.settlementNo)
        set('.settle-status',settlement.status==='COMPLETED'?'已结算':settlement.status)
        set('.settle-amount',`¥${Number(settlement.netAmount||0).toFixed(2)}`)
        const info=document.querySelectorAll('.settle-info');if(info[0])info[0].textContent=`结算时间：${settlement.updatedAt||settlement.createdAt||''}`;if(info[1])info[1].textContent=`工作日期：${String(order.endTime||'').slice(0,10)}`
        const feeValues=document.querySelectorAll('.fee-row .value');if(feeValues[0])feeValues[0].textContent=`${order.startTime||''} ~ ${order.endTime||''}`;if(feeValues[1])feeValues[1].textContent=`${order.unitPrice||0}元/${order.settlementType==='MONTH'?'月':'天'}`;if(feeValues[4])feeValues[4].textContent=`¥${Number(settlement.netAmount||0).toFixed(2)}`
        const detailValues=document.querySelectorAll('.detail-row .value');if(detailValues[0])detailValues[0].textContent=settlement.status==='COMPLETED'?'已结算':settlement.status;if(detailValues[1])detailValues[1].textContent=order.settlementType==='MONTH'?'月结':'当日结算';if(detailValues[2])detailValues[2].textContent='快马认证雇主';if(detailValues[3])detailValues[3].textContent=order.address||''
        set('.account-name','快马钱包');set('.account-desc',`可用余额 ¥${Number(wallet.availableBalance||0).toFixed(2)} · 冻结 ¥${Number(wallet.frozenBalance||0).toFixed(2)}`)
        window.__KUAIMA_API_DATA__={settlement,order,wallet};markLoaded(settlement)
      }catch(error){document.documentElement.dataset.apiLoaded='false';console.error('Settlement prototype API load failed',error)}
      return
    }
    if(role==='boss'&&page==='boss-publish'){
      const headers={'Content-Type':'application/json','X-User-Id':'3001'}
      const sectionByLabel=label=>[...document.querySelectorAll('.form-section')].find(section=>(section.querySelector('.form-label')?.textContent||'').includes(label))
      const payload=()=>{
        const tomorrow=new Date();tomorrow.setDate(tomorrow.getDate()+1);const day=`${tomorrow.getFullYear()}-${String(tomorrow.getMonth()+1).padStart(2,'0')}-${String(tomorrow.getDate()).padStart(2,'0')}`
        const settlementLabel=sectionByLabel('薪资待遇')?.querySelectorAll('.tag-group')[0]?.querySelector('.tag-item.active')?.textContent?.trim()||'日结'
        const settlementMap={'日结':'DAY','周结':'PRESS','月结':'MONTH','完工结算':'COMPLETION'}
        return {title:sectionByLabel('岗位名称')?.querySelector('input')?.value?.trim()||'临时岗位',jobType:sectionByLabel('岗位类型')?.querySelector('.tag-item.active')?.textContent?.trim()||'其他',salaryType:'DAILY',unitPrice:Number(sectionByLabel('薪资待遇')?.querySelector('input[type="number"]')?.value||0),headcount:Number(document.getElementById('peopleCount')?.value||1),settlementType:settlementMap[settlementLabel]||'DAY',cityCode:'310117',address:sectionByLabel('工作地点')?.querySelector('input[type="text"]')?.value?.trim()||'上海市松江区',startTime:`${day}T${sectionByLabel('工作时间')?.querySelectorAll('input[type="time"]')[0]?.value||'08:00'}:00`,endTime:`${day}T${sectionByLabel('工作时间')?.querySelectorAll('input[type="time"]')[1]?.value||'18:00'}:00`,description:sectionByLabel('岗位描述')?.querySelector('textarea')?.value?.trim()||'按现场负责人安排完成工作'}
      }
      const create=async submit=>{try{const createResult=await fetch('/api/v1/boss/jobs',{method:'POST',headers,body:JSON.stringify(payload())}).then(r=>r.json());if(createResult.code!=='0')throw new Error(createResult.message);if(submit){const submitResult=await fetch(`/api/v1/boss/jobs/${createResult.data}/submit`,{method:'POST',headers}).then(r=>r.json());if(submitResult.code!=='0')throw new Error(submitResult.message)}showResult(submit?'发布成功，已提交审核':'草稿保存成功');if(submit)setTimeout(()=>{top.location.href=`/boss/publish-success?id=${createResult.data}`},500)}catch(error){showResult(error.message||'保存失败',false)}}
      window.saveDraft=()=>create(false);window.publishJob=()=>create(true);markLoaded({form:'boss-job-create'});window.__KUAIMA_API_DATA__={form:'boss-job-create'}
      return
    }
    if(role==='boss'&&page==='boss-order'){
      try{
        const headers={'X-User-Id':'3001'}
        const [jobsResult,ordersResult]=await Promise.all([fetch('/api/v1/boss/jobs',{headers}).then(r=>r.json()),fetch('/api/v1/boss/orders',{headers}).then(r=>r.json())])
        const jobs=jobsResult.data||[],orders=ordersResult.data||[],statusMap={APPLIED:'recruiting',HIRED:'ended',ARRIVED:'settling',COMPLETED:'settling',SETTLED:'completed',CANCELLED:'cancelled'}
        const list=orders.map(order=>{const job=jobs.find(item=>String(item.id)===String(order.jobId))||{};return {id:order.id,orderId:order.id,jobId:order.jobId,title:order.title||job.title||order.orderNo,workTime:`${order.startTime||job.startTime||''} ~ ${order.endTime||job.endTime||''}`,location:order.address||job.address||'',wage:`${order.unitPrice||job.unitPrice||0}元/${order.settlementType==='MONTH'?'月':'天'}`,recruitCount:job.headcount||1,currentApply:orders.filter(item=>String(item.jobId)===String(order.jobId)).length,status:statusMap[order.status]||'recruiting',rawStatus:order.status}})
        window.__KUAIMA_API_DATA__={jobs,orders};window.__KUAIMA_BOSS_ORDER_LIST__=list
        window.eval("jobList.splice(0,jobList.length,...window.__KUAIMA_BOSS_ORDER_LIST__);renderList('all')")
        window.openBossOrderPage=(pageName,id)=>{top.location.href=`/boss/${pageName}?orderId=${id}`}
        window.runBossOrderAction=async(id,action)=>{try{const response=await fetch(`/api/v1/boss/orders/${id}/${action}`,{method:'POST',headers});const result=await response.json();if(result.code!=='0')throw new Error(result.message);showResult('操作成功');setTimeout(()=>location.reload(),450)}catch(error){showResult(error.message||'操作失败',false)}}
        window.getActions=function(job){if(job.rawStatus==='APPLIED')return `<button class="job-btn btn-primary" onclick="openBossOrderPage('applicant-info','${job.id}')">查看报名</button><button class="job-btn btn-secondary" onclick="runBossOrderAction('${job.id}','hire')">录用</button><button class="job-btn btn-danger" onclick="cancelJob('${job.id}')">取消招工</button>`;if(job.rawStatus==='HIRED')return `<button class="job-btn btn-secondary" onclick="confirmArrival('${job.id}')">确认到岗</button><button class="job-btn btn-link" onclick="openBossOrderPage('applicant-info','${job.id}')">详情</button>`;if(job.rawStatus==='ARRIVED')return `<button class="job-btn btn-primary" onclick="runBossOrderAction('${job.id}','complete')">确认完成</button><button class="job-btn btn-link" onclick="openBossOrderPage('publish-info','${job.id}')">详情</button>`;if(job.rawStatus==='COMPLETED')return `<button class="job-btn btn-primary" onclick="openBossOrderPage('suspend-settle','${job.id}')">去结算</button><button class="job-btn btn-link" onclick="openBossOrderPage('publish-info','${job.id}')">详情</button>`;return `<button class="job-btn btn-link" onclick="openBossOrderPage('publish-info','${job.id}')">详情</button>`}
        window.confirmCancel=async function(){document.getElementById('cancelModal').style.display='none';if(cancelTargetId)await window.runBossOrderAction(cancelTargetId,'cancel')}
        window.confirmArrive=async function(){document.getElementById('confirmModal').style.display='none';if(arriveTargetId)await window.runBossOrderAction(arriveTargetId,'arrived')}
        window.renderList('all');markLoaded(orders)
      }catch(error){document.documentElement.dataset.apiLoaded='false';console.error('Boss order prototype API load failed',error)}
      return
    }
    if(role==='boss'&&page==='suspend-settle'){
      try{
        const headers={'X-User-Id':'3001'},result=await fetch('/api/v1/boss/orders',{headers}).then(r=>r.json()),completed=(result.data||[]).filter(order=>order.status==='COMPLETED')
        const mapped=completed.map((order,index)=>({id:index+1,backendId:String(order.id),job:order.title,date:String(order.endTime||'').slice(5,10).replace('-','月')+'日',workers:[`零工${order.workerId}`],workerCount:1,amount:Number(order.unitPrice||0),status:'waiting',statusText:'待结算'}))
        window.__KUAIMA_SETTLE_ORDERS__=mapped;window.eval("orders.splice(0,orders.length,...window.__KUAIMA_SETTLE_ORDERS__);selectedIds.clear();renderOrders()")
        const total=mapped.reduce((sum,item)=>sum+item.amount,0),summary=document.querySelector('.summary-amount');if(summary)summary.textContent=`¥${total.toFixed(2)}`;const summaryDesc=document.querySelector('.summary-desc');if(summaryDesc)summaryDesc.textContent=`共${mapped.length}笔订单 · 涉及${mapped.reduce((sum,item)=>sum+item.workerCount,0)}位零工`;const totalCount=document.getElementById('total-count');if(totalCount)totalCount.textContent=String(mapped.length)
        window.settleSelected=function(){const chosen=window.orders.filter(item=>window.selectedIds.has(item.id));if(!chosen.length)return;const amount=chosen.reduce((sum,item)=>sum+item.amount,0);top.location.href=`/boss/settle-confirm?amount=${amount.toFixed(2)}&count=${chosen.length}&ids=${chosen.map(item=>item.backendId).join(',')}`}
        window.__KUAIMA_API_DATA__=completed;markLoaded(completed)
      }catch(error){document.documentElement.dataset.apiLoaded='false';console.error('Boss pending settlement load failed',error)}
      return
    }
    if(role==='boss'&&page==='settle-confirm'){
      try{
        const headers={'X-User-Id':'3001'},paramsIds=(params.get('ids')||'').split(',').filter(Boolean);let ids=paramsIds
        if(!ids.length){const listResult=await fetch('/api/v1/boss/orders',{headers}).then(r=>r.json());ids=(listResult.data||[]).filter(order=>order.status==='COMPLETED').slice(0,1).map(order=>String(order.id))}
        const details=(await Promise.all(ids.map(id=>fetch(`/api/v1/boss/orders/${id}`,{headers}).then(r=>r.json())))).map(result=>result.data).filter(Boolean)
        const gross=details.reduce((sum,item)=>sum+Number(item.unitPrice||0),0),fee=gross*.1,finalAmount=gross+fee
        window.settleData={amount:gross,count:details.length,orderIds:ids,finalAmount}
        const set=(id,value)=>{const el=document.getElementById(id);if(el)el.textContent=value};set('settle-amount',`¥${finalAmount.toFixed(2)}`);set('settle-desc',`共${details.length}笔订单 · 待付款`);set('detail-order-amount',`¥${gross.toFixed(2)}`);set('detail-final-amount',`¥${finalAmount.toFixed(2)}`)
        window.submitSettle=async function(){try{for(const detail of details){const response=await fetch(`/api/v1/boss/settlements/${detail.id}/confirm?grossAmount=${Number(detail.unitPrice||0).toFixed(2)}`,{method:'POST',headers:{...headers,'Idempotency-Key':`web-${detail.id}-${Date.now()}`}});const result=await response.json();if(result.code!=='0')throw new Error(result.message)}set('success-desc',`付款金额 ¥${finalAmount.toFixed(2)} 已成功支付`);document.getElementById('success-modal')?.classList.add('show');showResult('结算成功')}catch(error){showResult(error.message||'结算失败',false)}}
        document.querySelectorAll('#account-form input').forEach(input=>input.addEventListener('input',()=>window.updateConfirmBtn()))
        window.__KUAIMA_API_DATA__=details;markLoaded(details)
      }catch(error){document.documentElement.dataset.apiLoaded='false';console.error('Boss settlement confirm load failed',error)}
      return
    }
    if(role==='boss'&&page==='boss-home'){
      try{const headers={'X-User-Id':'3001'};const [jobs,orders]=await Promise.all([fetch('/api/v1/boss/jobs',{headers}).then(r=>r.json()),fetch('/api/v1/boss/orders',{headers}).then(r=>r.json())]);window.__KUAIMA_API_DATA__={jobs:jobs.data||[],orders:orders.data||[]};markLoaded([...(jobs.data||[]),...(orders.data||[])]);const highlights=document.querySelectorAll('.info-highlight,.stat-number');if(highlights[0])highlights[0].textContent=`${(jobs.data||[]).filter(x=>x.status==='ACTIVE').length}个岗位`;if(highlights[1])highlights[1].textContent=`${(orders.data||[]).filter(x=>['APPLIED','HIRED','ARRIVED'].includes(x.status)).length}个订单`}catch(error){document.documentElement.dataset.apiLoaded='false'}return
    }
    if((role==='worker'&&page==='profile')||(role==='boss'&&page==='boss-profile')){
      try{const headers={'X-User-Id':role==='boss'?'3001':'2001'};const [profileResult,walletResult]=await Promise.all([fetch(`/api/v1/${role}/profile`,{headers}).then(r=>r.json()),role==='worker'?fetch('/api/v1/worker/wallet',{headers}).then(r=>r.json()):Promise.resolve({data:null})]);const profile=profileResult.data||{},wallet=walletResult.data;window.__KUAIMA_API_DATA__={profile,wallet};markLoaded(wallet?[profile,wallet]:profile);const name=document.querySelector('.user-name,.profile-name,.name');if(name)name.textContent=`${profile.nickname||profile.realName||'快马用户'}｜${role==='boss'?'老板':'零工'}`;const phone=document.querySelector('.user-phone,.phone');if(phone)phone.textContent=profile.phone||'';const balance=document.querySelector('.wallet-value');if(balance&&wallet)balance.textContent=Number(wallet.availableBalance||0).toFixed(2)}catch(error){document.documentElement.dataset.apiLoaded='false'}return
    }
    if(page==='realname'){
      const userId=role==='boss'?'3001':'2001',headers={'X-User-Id':userId}
      try{
        const result=await fetch(`/api/v1/${role}/phone-verification`,{headers}).then(response=>response.json())
        if(result.code!=='0')throw new Error(result.message)
        let verification=result.data||{},sending=false,timerId=null,seconds=0
        const codeInput=document.getElementById('codeInput'),sendButton=document.getElementById('sendCodeBtn'),verifyButton=document.getElementById('verifyBtn')
        const phoneInput=document.querySelector('.form-input.phone')
        const description=document.querySelector('.header-desc,.page-desc')
        const paint=()=>{
          if(phoneInput)phoneInput.value=verification.maskedPhone||''
          if(description)description.innerHTML=verification.verified?`您的绑定手机号 ${verification.maskedPhone||''} 已完成验证`:`为了保障您的账户安全，需要验证<br>您绑定的手机号 ${verification.maskedPhone||''}`
          if(verification.verified){
            if(sendButton){sendButton.textContent='已验证';sendButton.disabled=true;sendButton.classList.add('disabled')}
            if(verifyButton){verifyButton.textContent='已完成验证';verifyButton.disabled=true}
            const boxes=document.querySelector('.code-input-wrap');if(boxes)boxes.innerHTML='<div style="color:#52c41a;font-size:15px;padding:13px 0"><i class="fas fa-circle-check" style="margin-right:7px"></i>当前账号已通过手机号验证</div>'
          }
        }
        const beginCountdown=value=>{
          clearInterval(timerId);seconds=Math.max(1,Number(value)||60)
          if(sendButton){sendButton.disabled=true;sendButton.classList.add('disabled')}
          const tick=()=>{if(!sendButton)return;sendButton.textContent=seconds>0?`${seconds}s 后重试`:'重新获取';if(seconds<=0){clearInterval(timerId);sendButton.disabled=false;sendButton.classList.remove('disabled');return}seconds--}
          tick();timerId=setInterval(tick,1000)
        }
        window.sendCode=async function(){
          if(sending||seconds>0||verification.verified)return
          sending=true
          try{const response=await fetch(`/api/v1/${role}/phone-verification/code`,{method:'POST',headers});const codeResult=await response.json();if(codeResult.code!=='0')throw new Error(codeResult.message);beginCountdown(codeResult.data.resendAfterSeconds);showResult(codeResult.data.demoCode?`演示验证码：${codeResult.data.demoCode}`:'验证码已发送');codeInput?.focus()}catch(error){showResult(error.message||'验证码发送失败',false)}finally{sending=false}
        }
        window.doVerify=async function(){
          const agreement=document.getElementById('agreementCheck')||document.getElementById('agreeCheckbox')
          if(agreement&&!agreement.classList.contains('checked')){showResult('请先阅读并同意隐私政策',false);return}
          const code=(codeInput?.value||'').replace(/\D/g,'').slice(0,6)
          if(code.length!==6){showResult('请输入6位验证码',false);return}
          try{const response=await fetch(`/api/v1/${role}/phone-verification/confirm`,{method:'POST',headers:{...headers,'Content-Type':'application/json'},body:JSON.stringify({code})});const confirmResult=await response.json();if(confirmResult.code!=='0')throw new Error(confirmResult.message);verification=confirmResult.data;paint();showResult('手机号验证成功')}catch(error){showResult(error.message||'验证码错误',false)}
        }
        window.doAuth=window.doVerify;window.startVerify=window.doVerify
        window.__KUAIMA_API_DATA__=verification;paint();markLoaded(verification)
      }catch(error){document.documentElement.dataset.apiLoaded='false';showResult(error.message||'手机号认证加载失败',false)}
      return
    }
    if(role==='worker'&&page==='job-detail'&&params.get('id')){
      try{const response=await fetch(`/api/v1/worker/jobs/${params.get('id')}`,{headers:{'X-User-Id':'2001'}});const result=await response.json();applyApiData(result.data);window.__KUAIMA_API_DATA__=result.data;markLoaded(result.data);document.addEventListener('click',async event=>{const button=event.target.closest('button,.apply-btn,.signup-btn');if(!button||!/报名|抢单/.test(button.textContent||''))return;event.preventDefault();event.stopImmediatePropagation();try{const applyResponse=await fetch(`/api/v1/worker/orders/apply/${params.get('id')}`,{method:'POST',headers:{'X-User-Id':'2001'}});const applyResult=await applyResponse.json();if(applyResult.code!=='0')throw new Error(applyResult.message);showResult('报名成功');button.textContent='已报名';button.disabled=true}catch(error){showResult(error.message||'报名失败',false)}},true)}catch(error){document.documentElement.dataset.apiLoaded='false'}
      return
    }
    if(!typeMap[page])return
    const type=typeMap[page]
    const url=type==='WALLET'?'/api/v1/worker/wallet':`/api/v1/${publicTypes.has(type)?'public':role}/resources/${type}`
    try{const response=await fetch(url,{headers:{'X-User-Id':role==='boss'?'3001':'2001'}});const result=await response.json();window.__KUAIMA_API_DATA__=result.data;markLoaded(result.data);applyApiData(result.data);bindGenericSave(type,url,result.data);window.dispatchEvent(new CustomEvent('kuaima-api-data',{detail:result.data}))}catch(error){document.documentElement.dataset.apiLoaded='false';console.error('Prototype API load failed',error)}
  }
  document.addEventListener('DOMContentLoaded',function(){removeDeviceChrome();fitViewport();bindAppNavigation();loadApiData()})
})()
