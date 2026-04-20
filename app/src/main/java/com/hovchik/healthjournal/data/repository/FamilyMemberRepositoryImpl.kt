package com.hovchik.healthjournal.data.repository

import com.hovchik.healthjournal.data.local.JsonFileStore
import com.hovchik.healthjournal.data.local.dto.FamilyMemberDto
import com.hovchik.healthjournal.domain.model.FamilyMember
import com.hovchik.healthjournal.domain.repository.FamilyMemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FamilyMemberRepositoryImpl(
    private val store: JsonFileStore<FamilyMemberDto>
) : FamilyMemberRepository {

    override fun getAllMembers(): Flow<List<FamilyMember>> =
        store.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getMemberById(id: Long): FamilyMember? =
        store.getById(id)?.toDomain()

    override suspend fun insertMember(member: FamilyMember): Long =
        store.insert(FamilyMemberDto.from(member))

    override suspend fun updateMember(member: FamilyMember) =
        store.update(FamilyMemberDto.from(member))

    override suspend fun deleteMember(member: FamilyMember) =
        store.delete(FamilyMemberDto.from(member))
}
