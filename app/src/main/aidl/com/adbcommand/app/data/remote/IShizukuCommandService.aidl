package com.adbcommand.app.data.remote;

import android.os.Bundle;

interface IShizukuCommandService {
    Bundle runCommand(in String cmd);
}
