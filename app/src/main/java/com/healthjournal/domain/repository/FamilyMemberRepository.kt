package com.healthjournal.domain.repository

import com.healthjournal.domain.model.FamilyMember
import kotlinx.coroutines.flow.Flow

interface FamilyMemberRepository {
    fun getAllMembers(): Flow<List<FamilyMember>>
    suspend fun getMemberById(id: Long): FamilyMember?
    suspend fun insertMember(member: FamilyMember): Long
    suspend fun updateMember(member: FamilyMember)
    suspend fun deleteMember(member: FamilyMember)
}
