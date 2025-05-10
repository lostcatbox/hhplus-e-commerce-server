package com.example.server.service

import com.example.server.dto.PopularProductResponse
import com.example.server.repository.PopularProductRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PopularProductService(
    private val popularProductRepository: PopularProductRepository
) {
    @Cacheable(
        value = ["popularProducts"],
        key = "#period + ':' + #category + ':' + #page + ':' + #size",
        unless = "#result.isEmpty()"
    )
    fun getPopularProducts(
        period: String,
        category: String?,
        page: Int,
        size: Int
    ): List<PopularProductResponse> {
        return popularProductRepository.findPopularProducts(period, category, page, size)
    }
} 