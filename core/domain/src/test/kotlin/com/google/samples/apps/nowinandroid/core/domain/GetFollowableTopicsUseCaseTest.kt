/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.core.domain

import com.google.samples.apps.nowinandroid.core.domain.TopicSortField.FOLLOWED
import com.google.samples.apps.nowinandroid.core.domain.TopicSortField.NAME
import com.google.samples.apps.nowinandroid.core.model.data.FollowableTopic
import com.google.samples.apps.nowinandroid.core.model.data.Topic
import com.google.samples.apps.nowinandroid.core.testing.repository.TestTopicsRepository
import com.google.samples.apps.nowinandroid.core.testing.repository.TestUserDataRepository
import com.google.samples.apps.nowinandroid.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GetFollowableTopicsUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val topicsRepository = TestTopicsRepository()
    private val userDataRepository = TestUserDataRepository()

    val useCase = GetFollowableTopicsUseCase(
        topicsRepository,
        userDataRepository,
    )

    @Test
    fun whenNoParams_followableTopicsAreReturnedWithNoSorting() = runTest {
        // Obtain a stream of followable topics.
        val followableTopics = useCase()

        // Send some test topics and their followed state.
        topicsRepository.sendTopics(testTopics)
        userDataRepository.setFollowedTopicIds(setOf(testTopics[0].id, testTopics[2].id))

        // Check that the order hasn't changed and that the correct topics are marked as followed.
        assertEquals(
            listOf(
                FollowableTopic(testTopics[0], true),
                FollowableTopic(testTopics[1], false),
                FollowableTopic(testTopics[2], true),
            ),
            followableTopics.first(),
        )
    }

    @Test
    fun whenSortOrderIsByName_topicsSortedByNameAreReturned() = runTest {
        // Obtain a stream of followable topics, sorted by name.
        val followableTopics = useCase(
            sortBy = NAME,
        )

        // Send some test topics and their followed state.
        topicsRepository.sendTopics(testTopics)
        userDataRepository.setFollowedTopicIds(setOf())

        // Check that the followable topics are sorted by the topic name.
        assertEquals(
            followableTopics.first(),
            testTopics
                .sortedBy { it.name }
                .map {
                    FollowableTopic(it, false)
                },
        )
    }

    @Test
    fun whenSortOrderIsByFollowed_followedTopicsFirstThenEachGroupSortedByNameCaseInsensitively() = runTest {
        val followableTopics = useCase(sortBy = FOLLOWED)

        topicsRepository.sendTopics(mixedCaseTopics)
        userDataRepository.setFollowedTopicIds(setOf("1", "4", "6"))

        assertEquals(
            listOf(
                FollowableTopic(mixedCaseTopics[5], true),
                FollowableTopic(mixedCaseTopics[0], true),
                FollowableTopic(mixedCaseTopics[3], true),
                FollowableTopic(mixedCaseTopics[4], false),
                FollowableTopic(mixedCaseTopics[1], false),
                FollowableTopic(mixedCaseTopics[2], false),
            ),
            followableTopics.first(),
        )
    }

    @Test
    fun whenSortOrderIsByFollowed_andNoTopicsFollowed_topicsSortedByNameCaseInsensitively() = runTest {
        val followableTopics = useCase(sortBy = FOLLOWED)

        topicsRepository.sendTopics(mixedCaseTopics)
        userDataRepository.setFollowedTopicIds(setOf())

        assertEquals(
            listOf(
                FollowableTopic(mixedCaseTopics[4], false),
                FollowableTopic(mixedCaseTopics[1], false),
                FollowableTopic(mixedCaseTopics[5], false),
                FollowableTopic(mixedCaseTopics[0], false),
                FollowableTopic(mixedCaseTopics[2], false),
                FollowableTopic(mixedCaseTopics[3], false),
            ),
            followableTopics.first(),
        )
    }
}

private val testTopics = listOf(
    Topic("1", "Headlines", "", "", "", ""),
    Topic("2", "Android Studio", "", "", "", ""),
    Topic("3", "Compose", "", "", "", ""),
)

private val mixedCaseTopics = listOf(
    Topic("1", "compose", "", "", "", ""),
    Topic("2", "Android Studio", "", "", "", ""),
    Topic("3", "headlines", "", "", "", ""),
    Topic("4", "Kotlin", "", "", "", ""),
    Topic("5", "accessibility", "", "", "", ""),
    Topic("6", "Benchmarks", "", "", "", ""),
)
