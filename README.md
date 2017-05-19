# Awesome Possum
================================

A library meant to gather data about a user and upload to the cloud. In order to use,

To use, add the following to your app gradle dependencies:

    compile 'com.telenor:possumlib:1.2.5'
    
Remember to add jCenter() to your repositories.

To use this library, here is a short list of the commands you need:

     AwesomePossum.listen(Context);
     AwesomePossum.getAuthorizeDialog(ActivityContext,
                                      String Title, 
                                      String Message,
                                      StringOkText,
                                      String CancelText).show();
     AwesomePossum.requestPermissions(Context);
     AwesomePossum.authorizeGathering();
     AwesomePossum.stopListening(Context);
     AwesomePossum.startUpload(Context,
                               String AmazonBucketKey, 
                               Bool allowMobileUpload);

Remember to authorize before starting to listen, or it will throw a NotAuthorizedException.
This can be done either with the getAuthorizeDialog method that does all for you, or by manually
using the requestPermissions method as well as authorizeGathering method.

This is a work in progress.