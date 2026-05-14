function addReviewApi(data) {
  return $axios({
    url: '/orderReview',
    method: 'post',
    data
  })
}

function reviewPagingApi(data) {
  return $axios({
    url: '/orderReview/myPage',
    method: 'get',
    params: { ...data }
  })
}

function addAfterSaleApi(data) {
  return $axios({
    url: '/afterSale',
    method: 'post',
    data
  })
}

function afterSalePagingApi(data) {
  return $axios({
    url: '/afterSale/myPage',
    method: 'get',
    params: { ...data }
  })
}
