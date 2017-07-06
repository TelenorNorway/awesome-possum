package com.telenor.possumlib.constants;

public class Messaging {
    public static final String LEARNING = "toggleLearning";
    public static final String REQUEST_DETECTORS = "requestDetectors";
    public static final String DETECTORS = "detectors";
    public static final String DETECTORS_STATUS = "detectorsStatus";
    public static final String POSSUM_TRUST = "PossumTrust";

    // Messaging between service and client
    public static final String POSSUM_MESSAGE = "PossumMessage"; // Message sent from/to service
    public static final String POSSUM_MESSAGE_TYPE = "PossumType";
    public static final String UPLOAD_SUCCESS = "uploadSuccess";
    public static final String UPLOAD_FAILED = "uploadFailed";
    public static final String VERIFICATION_FAILED = "VerificationFailed";
    public static final String VERIFICATION_SUCCESS = "VerificationSuccess";
    public static final String COLLECTION_FAILED = "CollectionFailed";
    public static final String POSSUM_TERMINATE = "PossumTerminate";

    // Status messages
    public static final String ANALYSING = "Analysing";
    public static final String SENDING_RESULT = "SendingResult";
    public static final String READY_TO_AUTH = "ReadyForAuth";
    public static final String MISSING_VALID_ID = "MissingValidId";
    public static final String AUTH_DONE = "authDone";
}