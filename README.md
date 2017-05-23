# Awesome Possum

A library meant to gather data about a user and upload to the cloud. In order to use,

To use, add the following to your app gradle dependencies:

    compile 'com.telenor:possumlib:0.9.0'
    
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

License
====================

    Copyright 2017 Telenor Norge AS

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
