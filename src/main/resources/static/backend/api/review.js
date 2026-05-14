const getReviewPage = (params) => {
  return $axios({
    url: '/orderReview/page',
    method: 'get',
    params
  })
}

const replyReview = (params) => {
  return $axios({
    url: '/orderReview/reply',
    method: 'put',
    data: { ...params }
  })
}

const updateReviewStatus = (params) => {
  return $axios({
    url: '/orderReview/status',
    method: 'put',
    params
  })
}
