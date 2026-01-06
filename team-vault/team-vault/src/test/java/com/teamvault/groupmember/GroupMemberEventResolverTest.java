package com.teamvault.groupmember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teamvault.enums.GroupMemberEventType;
import com.teamvault.event.model.GroupMemberEvent;
import com.teamvault.event.model.MemberAcceptedInvite;
import com.teamvault.event.model.MemberExitedGroup;
import com.teamvault.event.model.MemberRejectedInvite;
import com.teamvault.event.resolver.GroupMemberEventResolver;

@ExtendWith(MockitoExtension.class)
class GroupMemberEventResolverTest {

    @Mock
    private MemberAcceptedInvite memberAcceptedInvite;

    @Mock
    private MemberRejectedInvite memberRejectedInvite;

    @Mock
    private MemberExitedGroup memberExitedGroup;

    @InjectMocks
    private GroupMemberEventResolver resolver;

    @BeforeEach
    void setUp() throws Exception {
    	
        Method initMethod = GroupMemberEventResolver.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(resolver);
    }

    @Test
    void resolve_shouldReturnCorrectHandlerForEachEventType() {

        GroupMemberEvent accepted = resolver.resolve(GroupMemberEventType.INVITE_ACCEPTED);

        GroupMemberEvent rejected = resolver.resolve(GroupMemberEventType.INVITE_REJECTED);

        GroupMemberEvent exited = resolver.resolve(GroupMemberEventType.MEMBER_EXITED);

        assertNotNull(accepted);
        assertNotNull(rejected);
        assertNotNull(exited);

        assertEquals(memberAcceptedInvite, accepted);
        assertEquals(memberRejectedInvite, rejected);
        assertEquals(memberExitedGroup, exited);
    }
}
