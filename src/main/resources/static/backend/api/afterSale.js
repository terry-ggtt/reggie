const getAfterSalePage = (params) => {
  return $axios({
    url: '/afterSale/page',
    method: 'get',
    params
  })
}

const handleAfterSale = (params) => {
  return $axios({
    url: '/afterSale/handle',
    method: 'put',
    data: { ...params }
  })
}
