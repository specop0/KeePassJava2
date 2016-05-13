/*
 * Copyright 2015 SpecOp0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package listener;

import java.util.EventListener;
import java.util.List;
import main.DatabaseObject;
import org.linguafranca.pwdb.Entry;

/**
 *
 * @author SpecOp0
 */
public interface SelectionChangedListener extends EventListener {

    void showData(DatabaseObject object);
    
    void showData(List<Entry> entries);

}
