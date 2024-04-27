package metanode.serialization.test;

import metanode.serialization.MessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the MessageType enum
 */

public class MessageTypeTest {


        private static final int VERSION = 0b0100; // Binary representation

        /**
         * Test that the message type is RequestNodes
         */

        @Test
        public void testRequestNodes() {
            int code = (VERSION << 4) | MessageType.RequestNodes.getCode();
            assertEquals(MessageType.RequestNodes, MessageType.getByCode(code));
        }

        /**
         * Test that the message type is RequestMetaNodes
         */

        @Test
        public void testRequestMetaNodes() {
            int code = (VERSION << 4) | MessageType.RequestMetaNodes.getCode();
            assertEquals(MessageType.RequestMetaNodes, MessageType.getByCode(code));
        }

        /**
         * Test that the message type is AnswerRequest
         */

        @Test
        public void testAnswerRequest() {
            int code = (VERSION << 4) | MessageType.AnswerRequest.getCode();
            assertEquals(MessageType.AnswerRequest, MessageType.getByCode(code));
        }

        /**
         * Test that the message type is NodeAdditions
         */

        @Test
        public void testNodeAdditions() {
            int code = (VERSION << 4) | MessageType.NodeAdditions.getCode();
            assertEquals(MessageType.NodeAdditions, MessageType.getByCode(code));
        }

        /**
         * Test that the message type is MetaNodeAdditions
         */

        @Test
        public void testMetaNodeAdditions() {
            int code = (VERSION << 4) | MessageType.MetaNodeAdditions.getCode();
            assertEquals(MessageType.MetaNodeAdditions, MessageType.getByCode(code));
        }

        /**
         * Test that the message type is NodeDeletions
         */

        @Test
        public void testNodeDeletions() {
            int code = (VERSION << 4) | MessageType.NodeDeletions.getCode();
            assertEquals(MessageType.NodeDeletions, MessageType.getByCode(code));
        }

        /**
         * Test that the message type is MetaNodeDeletions
         */

        @Test
        public void testMetaNodeDeletions() {
            int code = (VERSION << 4) | MessageType.MetaNodeDeletions.getCode();
            assertEquals(MessageType.MetaNodeDeletions, MessageType.getByCode(code));
        }
    }
